import importlib.util
import io
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
