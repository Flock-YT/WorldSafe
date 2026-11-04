import importlib.util
import io
import json
from pathlib import Path
import struct
import sys
import tempfile
import unittest
import zipfile


root = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(root / "scripts"))
spec = importlib.util.spec_from_file_location("verify_probe", root / "scripts/verify-fabric-probe.py")
verifier = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verifier)


def class_bytes(name, major=52):
    def text(value):
        encoded = value.encode()
        return b"\x01" + struct.pack(">H", len(encoded)) + encoded
    pool = text(name) + b"\x07\x00\x01" + text("java/lang/Object") + b"\x07\x00\x03"
    return struct.pack(">IHHH", 0xCAFEBABE, 0, major, 5) + pool + struct.pack(">HHHHHHH", 0x21, 2, 4, 0, 0, 0, 0)


class PackagingTest(unittest.TestCase):
    def inspect(self, entry, data, maximum=52, bootstrap=True):
        output = io.BytesIO()
        with zipfile.ZipFile(output, "w") as jar:
            jar.writestr(entry, data)
        with zipfile.ZipFile(io.BytesIO(output.getvalue())) as jar:
            verifier.inspect_classes(jar, maximum, bootstrap)

    def test_rejects_case_insensitive_filesystem_package_corruption(self):
        with self.assertRaisesRegex(ValueError, "case mismatch"):
            self.inspect("me/lele/worldsafe/Core.class", class_bytes("me/lele/worldSafe/Core"))

    def test_accepts_the_preserved_bukkit_namespace(self):
        self.inspect("me/lele/worldSafe/Core.class", class_bytes("me/lele/worldSafe/Core"))

    def test_rejects_modern_classes_in_the_bootstrap(self):
        with self.assertRaisesRegex(ValueError, "class version"):
            self.inspect("example/Modern.class", class_bytes("example/Modern", 69))

    def test_allows_modern_bytecode_inside_a_matching_runtime(self):
        self.inspect("example/Modern.class", class_bytes("example/Modern", 69), maximum=69, bootstrap=False)

    def test_rejects_bundled_platform_classes(self):
        with self.assertRaisesRegex(ValueError, "platform class"):
            self.inspect("net/minecraft/Entity.class", class_bytes("net/minecraft/Entity"))

    def test_rejects_dependency_signature_residue(self):
        with self.assertRaisesRegex(ValueError, "signature"):
            self.inspect("META-INF/DEPENDENCY.SF", b"signature")

    def test_rejects_an_accidentally_bundled_test_driver(self):
        name = "me/lele/worldSafe/fabric/driver/NativeFixture"
        with self.assertRaisesRegex(ValueError, "test driver"):
            self.inspect(name + ".class", class_bytes(name))

    def test_rejects_shared_classes_in_a_nested_runtime(self):
        name = "me/lele/worldSafe/core/ProtectionPolicy"
        with self.assertRaisesRegex(ValueError, "nested runtime"):
            self.inspect(name + ".class", class_bytes(name), bootstrap=False)

    def test_checks_all_shared_classes_in_the_outer_jar(self):
        from shared_artifact import shared_entries, verify_shared_core
        output = io.BytesIO()
        with zipfile.ZipFile(output, "w") as jar:
            for entry in shared_entries():
                jar.writestr(entry, class_bytes(entry[:-6]))
        with zipfile.ZipFile(io.BytesIO(output.getvalue())) as jar:
            verify_shared_core(jar)
        output = io.BytesIO()
        with zipfile.ZipFile(output, "w") as jar:
            jar.writestr("unrelated.txt", "missing core")
        with zipfile.ZipFile(io.BytesIO(output.getvalue())) as jar:
            with self.assertRaisesRegex(ValueError, "Missing shared core"):
                verify_shared_core(jar)


class LoaderMetadataTest(unittest.TestCase):
    def verify(self, shell_loader=">=0.19.5", runtime_loaders=(">=0.19.5", ">=0.19.5")):
        from shared_artifact import shared_entries
        games = ("1.14", "26.3")
        lock = {"loader": "0.20.1", "minimumLoader": "0.19.5", "targets": [
            {"minecraft": game, "java": java} for game, java in zip(games, (8, 25))]}
        shell = {"id": "worldsafe", "version": "1.1.2", "entrypoints": {"main": ["example.Shell"]},
                 "depends": {"java": ">=8", "worldsafe_runtime": "*", "minecraft": ["=" + game for game in games]},
                 "jars": [{"file": "META-INF/jars/" + game + ".jar"} for game in games]}
        if shell_loader is not None:
            shell["depends"]["fabricloader"] = shell_loader
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "universal.jar"
            with zipfile.ZipFile(path, "w") as jar:
                jar.writestr("fabric.mod.json", json.dumps(shell))
                jar.writestr("example/Shell.class", class_bytes("example/Shell"))
                for entry in shared_entries():
                    jar.writestr(entry, class_bytes(entry[:-6]))
                for target, loader, nested in zip(lock["targets"], runtime_loaders, shell["jars"]):
                    game, java = target["minecraft"], target["java"]
                    runtime = {"id": "worldsafe_runtime", "version": "1.1.2-mc." + game,
                               "entrypoints": {"main": ["example.Runtime"]},
                               "depends": {"minecraft": "=" + game, "java": ">=" + str(java)},
                               "custom": {"worldsafe:game": game}}
                    if loader is not None:
                        runtime["depends"]["fabricloader"] = loader
                    output = io.BytesIO()
                    with zipfile.ZipFile(output, "w") as module:
                        module.writestr("fabric.mod.json", json.dumps(runtime))
                        module.writestr("worldsafe.mixins.json", json.dumps({
                            "required": True, "package": "example", "mixins": ["NativeMixin"],
                            "injectors": {"defaultRequire": 1}}))
                        for name in ("Runtime", "NativeMixin"):
                            module.writestr("example/" + name + ".class", class_bytes("example/" + name, java + 44))
                    jar.writestr(nested["file"], output.getvalue())
            return verifier.verify(path, lock, require_all=True)

    def test_build_loader_can_differ_from_the_minimum_runtime_loader(self):
        self.assertTrue(self.verify()["allLockedTargetsPresent"])

    def test_rejects_missing_or_incorrect_shell_loader(self):
        for loader in (None, ">=0.20.1", ">=0.19.4", "*"):
            with self.subTest(loader=loader), self.assertRaisesRegex(ValueError, "Bootstrap Java/Loader"):
                self.verify(shell_loader=loader)

    def test_checks_every_nested_runtime_loader(self):
        for index in range(2):
            for loader in (None, ">=0.20.1", ">=0.19.4", "*"):
                loaders = [">=0.19.5", ">=0.19.5"]
                loaders[index] = loader
                with self.subTest(index=index, loader=loader), self.assertRaisesRegex(ValueError, "Runtime Loader"):
                    self.verify(runtime_loaders=loaders)


class UnitTestEvidenceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        spec = importlib.util.spec_from_file_location("code_verifier", root / "scripts/verify-fabric-code.py")
        cls.code_verifier = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.code_verifier)

    def test_shared_results_are_required_and_must_pass(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            with self.assertRaisesRegex(ValueError, "Shared unit-test results are missing"):
                self.code_verifier.collect_tests(directory, "Shared")
            report = directory / "TEST-Core.xml"
            for attribute in ("failures", "errors", "skipped"):
                report.write_text('<testsuite name="Core" tests="1" ' + attribute + '="1"/>')
                with self.assertRaisesRegex(ValueError, "failed or were skipped"):
                    self.code_verifier.collect_tests(directory, "Shared")
            report.write_text('<testsuite name="Core" tests="0"/>')
            with self.assertRaisesRegex(ValueError, "results are missing"):
                self.code_verifier.collect_tests(directory, "Shared")
            report.write_text('<testsuite name="Core" tests="7" failures="0" errors="0" skipped="0"/>')
            self.assertEqual([{"class": "Core", "tests": 7}], self.code_verifier.collect_tests(directory, "Shared"))


if __name__ == "__main__":
    unittest.main()
