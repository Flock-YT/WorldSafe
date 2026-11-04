import contextlib
import importlib.util
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch


ROOT = Path(__file__).resolve().parents[2]


def load_probe(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / "scripts" / (name + ".py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class LoaderProbeTest(unittest.TestCase):
    def test_both_probe_clis_record_and_isolate_explicit_and_default_loaders(self):
        for name, environment in (("run-fabric-architecture-probe", "server"),
                                  ("run-fabric-client-architecture-probe", "client")):
            module = load_probe(name)
            helper = module if environment == "server" else module.helpers
            with self.subTest(probe=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                (root / "fabric").mkdir()
                (root / "fabric/versions.lock.json").write_text(json.dumps({"loader": "0.20.1"}))
                argv = [name, "--game", "26.3", "--jar", "universal.jar", "--java-home", "jdk",
                        "--driver", "driver.jar"]

                def launch(args, report):
                    work = helper.probe_path("runs", args, environment)
                    work.mkdir(parents=True)
                    (work / "marker").write_text(args.loader)
                    report["passed"] = True

                with contextlib.ExitStack() as stack:
                    stack.enter_context(patch.object(module, "ROOT", root))
                    stack.enter_context(patch.object(helper, "ROOT", root))
                    stack.enter_context(patch.object(module, "launch", side_effect=launch))
                    stack.enter_context(contextlib.redirect_stdout(io.StringIO()))
                    for loader, options in (("0.19.5", ["--loader", "0.19.5"]), ("0.20.1", [])):
                        with patch("sys.argv", argv + options), self.assertRaises(SystemExit) as exit_code:
                            module.main()
                        self.assertEqual(0, exit_code.exception.code)
                        report = root / "fabric/build/architecture-reports" / loader / ("26.3-" + environment + ".json")
                        self.assertEqual(loader, json.loads(report.read_text())["loader"])
                    for loader in ("0.19.5", "0.20.1"):
                        marker = root / "fabric/build/architecture-runs" / loader / ("26.3-" + environment) / "marker"
                        self.assertEqual(loader, marker.read_text())

    def test_loader_cannot_escape_its_run_directory(self):
        import argparse
        module = load_probe("run-fabric-architecture-probe")
        for loader in ("../0.19.5", "/tmp", "", "0.19.5/../../"):
            with self.subTest(loader=loader), self.assertRaises(argparse.ArgumentTypeError):
                module.loader_version(loader)


if __name__ == "__main__":
    unittest.main()
