import copy
import hashlib
import importlib.util
from pathlib import Path
import re
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("acceptance", ROOT / "scripts/verify-fabric-report.py")
acceptance = importlib.util.module_from_spec(spec)
spec.loader.exec_module(acceptance)


class AcceptanceReportTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.jar = Path(self.directory.name) / "universal.jar"
        self.jar.write_bytes(b"the artifact used by every native run")
        self.digest = hashlib.sha256(self.jar.read_bytes()).hexdigest()
        source = (ROOT / "shared/src/main/java/me/lele/worldSafe/core/FeatureCatalog.java").read_text()
        catalog = dict(re.findall(r'add\(features, "([A-Za-z]+)", MinecraftVersion\.V([0-9_]+)\);', source))
        features = {}
        results = []
        applicable = []
        for key, minimum in catalog.items():
            minimum = minimum.replace("_", ".")
            active = acceptance.version("1.14") >= acceptance.version(minimum)
            features[key] = dict(minimumVersion=minimum, applicable=active,
                                 status="PASSED" if active else "NOT_APPLICABLE")
            if active:
                applicable.append(key)
                for scenario in ("disabled-positive-control", "enabled", "unconfigured-dimension-positive-control"):
                    results.append(dict(feature=key, scenario=scenario, passed=True))
        behavior = dict(schemaVersion=2, complete=True, untestedFeatures=[], untestedMechanisms=[],
                        features=features, featureKeysCovered=applicable, results=results)
        run = dict(jarSha256=self.digest, passed=True, matrixPassed=True, exitCode=0,
                   missingMarkers=[], timeout=False, behavior=behavior)
        self.lock = dict(targets=[dict(minecraft="1.14")])
        self.report = dict(jarSha256=self.digest, artifactUnchanged=True, complete=True, passed=True,
                           developmentSmokeOnly=False, targets=["1.14"],
                           results=[dict(minecraft="1.14", environments=dict(server=run, client=copy.deepcopy(run)))])

    def run_report(self):
        return acceptance.verify(self.jar, self.report, self.lock)

    def test_accepts_a_complete_same_artifact_contract(self):
        self.assertEqual(self.digest, self.run_report())

    def test_startup_only_is_insufficient(self):
        self.report["results"][0]["environments"]["client"]["behavior"] = {}
        with self.assertRaisesRegex(ValueError, "report schema"):
            self.run_report()

    def test_smoke_cannot_authorize_a_release_even_when_successful(self):
        self.report["developmentSmokeOnly"] = True
        with self.assertRaisesRegex(ValueError, "smoke"):
            self.run_report()

    def test_rebuilding_after_tests_invalidates_the_evidence(self):
        self.jar.write_bytes(b"a later rebuilt artifact")
        with self.assertRaisesRegex(ValueError, "unchanged JAR"):
            self.run_report()

    def test_a_different_hash_in_one_environment_is_rejected(self):
        self.report["results"][0]["environments"]["client"]["jarSha256"] = "0" * 64
        with self.assertRaisesRegex(ValueError, "mismatched"):
            self.run_report()

    def test_missing_integrated_server_run_is_rejected(self):
        del self.report["results"][0]["environments"]["client"]
        with self.assertRaises(ValueError):
            self.run_report()

    def test_missing_positive_control_is_rejected(self):
        behavior = self.report["results"][0]["environments"]["server"]["behavior"]
        behavior["results"] = [case for case in behavior["results"]
                               if not (case["feature"] == "tntExplosionCancel" and case["scenario"] == "disabled-positive-control")]
        with self.assertRaisesRegex(ValueError, "world controls"):
            self.run_report()

    def test_failing_native_assertion_is_rejected(self):
        self.report["results"][0]["environments"]["server"]["behavior"]["results"][0]["passed"] = False
        with self.assertRaisesRegex(ValueError, "assertion failed"):
            self.run_report()

    def test_untested_source_variant_is_rejected(self):
        self.report["results"][0]["environments"]["server"]["behavior"]["untestedMechanisms"] = ["tntExplosionCancel/minecart-enabled"]
        with self.assertRaisesRegex(ValueError, "coverage"):
            self.run_report()

    def test_incorrect_not_applicable_claim_is_rejected(self):
        self.report["results"][0]["environments"]["client"]["behavior"]["features"]["tntExplosionCancel"]["status"] = "NOT_APPLICABLE"
        with self.assertRaisesRegex(ValueError, "feature result"):
            self.run_report()


if __name__ == "__main__":
    unittest.main()
