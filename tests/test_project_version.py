import importlib.util
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))
from project_version import parse_properties

spec = importlib.util.spec_from_file_location("detect_release", ROOT / "scripts/detect-release.py")
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ProjectVersionTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.git("init", "-q")
        self.git("config", "user.name", "WorldSafe test")
        self.git("config", "user.email", "worldsafe@example.invalid")
        self.git("config", "commit.gpgsign", "false")
        self.git("config", "core.hooksPath", str(self.root / "no-hooks"))

    def git(self, *args):
        return subprocess.run(["git", "-C", str(self.root), *args], check=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True).stdout.strip()

    def commit(self, version, legacy=False):
        if legacy:
            (self.root / "pom.xml").write_text('<project xmlns="http://maven.apache.org/POM/4.0.0">'
                                              '<version>' + version + '</version></project>')
        else:
            (self.root / "gradle.properties").write_text("version=" + version + "\n")
        self.git("add", ".")
        self.git("commit", "-qm", "Version fixture")
        return self.git("rev-parse", "HEAD")

    def detect(self, before, current="1.1.1", event="push", ref="refs/heads/master"):
        (self.root / "gradle.properties").write_text("version=" + current + "\n")
        return release.detect(self.root, event, before, ref)

    def test_migration_with_same_version_does_not_release(self):
        before = self.commit("1.1.1", legacy=True)
        self.assertEqual("false", self.detect(before)["should_release"])

    def test_migration_with_a_new_version_does_release(self):
        before = self.commit("1.1.0", legacy=True)
        self.assertEqual("true", self.detect(before)["should_release"])

    def test_normal_upgrade_uses_gradle_version(self):
        before = self.commit("1.1.1")
        result = self.detect(before, "1.1.2-rc.1")
        self.assertEqual("true", result["should_release"])
        self.assertEqual("v1.1.2-rc.1", result["tag"])
        self.assertEqual("WorldSafe-1.1.2-rc.1.jar", result["artifact"])

    def test_non_version_properties_changes_do_not_release(self):
        before = self.commit("1.1.1")
        (self.root / "gradle.properties").write_text("org.gradle.parallel=false\nversion=1.1.1\n")
        self.assertEqual("false", release.detect(self.root, "push", before, "refs/heads/master")["should_release"])

    def test_invalid_current_version_fails(self):
        before = self.commit("1.1.1")
        with self.assertRaises(ValueError):
            self.detect(before, "../invalid")

    def test_invalid_historical_properties_never_fall_back_to_pom(self):
        self.commit("1.1.1", legacy=True)
        before = self.commit("../invalid")
        with self.assertRaises(ValueError):
            self.detect(before)

    def test_missing_history_fails(self):
        with self.assertRaises(subprocess.CalledProcessError):
            self.detect("f" * 40)

    def test_initial_push_and_manual_master_release(self):
        self.assertEqual("true", self.detect("0" * 40)["should_release"])
        self.assertEqual("true", self.detect("", event="workflow_dispatch")["should_release"])

    def test_manual_release_cannot_run_on_another_branch(self):
        with self.assertRaises(ValueError):
            self.detect("", event="workflow_dispatch", ref="refs/heads/other")

    def test_properties_parser_matches_the_gradle_declaration_contract(self):
        self.assertEqual("1.1.1", parse_properties("# version=ignored\r\nversion = 1.1.1\r\n"))
        for text in ["", "version=", "version=bad version", "version\n=1.1.1", "version=1\nversion=2", "version=1\nversion=", "version=1\n  version=2"]:
            with self.subTest(text=text), self.assertRaises(ValueError):
                parse_properties(text)

    def test_cli_writes_the_checked_outputs_without_build_tools(self):
        before = self.commit("1.1.1", legacy=True)
        (self.root / "gradle.properties").write_text("version=1.1.1\n")
        output = self.root / "outputs"
        environment = dict(os.environ, BEFORE_SHA=before, EVENT_NAME="push", GITHUB_REF="refs/heads/master", GITHUB_OUTPUT=str(output))
        result = subprocess.run([sys.executable, str(ROOT / "scripts/detect-release.py"), "--root", str(self.root)],
                                env=environment, capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("should_release=false\n", output.read_text())
        self.assertIn("version=1.1.1\n", output.read_text())
        output.unlink()
        (self.root / "gradle.properties").write_text("version=../invalid\n")
        result = subprocess.run([sys.executable, str(ROOT / "scripts/detect-release.py"), "--root", str(self.root)],
                                env=environment, capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
