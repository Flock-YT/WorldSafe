package me.lele.worldSafe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseWorkflowTest {
    @TempDir
    Path temporary;

    @Test
    void invalidReleaseInputsCannotInvokeGitHub() throws Exception {
        assertNotEquals(0, runRelease("../bad", temporary, digest(new byte[0])));
        assertFalse(Files.exists(temporary.resolve("gh-args")));
        assertNotEquals(0, runRelease("1.2.3", temporary, "invalid"));
        assertFalse(Files.exists(temporary.resolve("gh-args")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bash", "/bin/bash"})
    void releaseChannelsAndArtifactDigestAreCheckedBeforeCallingGitHub(String shell) throws Exception {
        for (String version : Arrays.asList("1.2.3", "1.2.3-beta.1", "1.2.3-alpha.1", "1.2.3-rc.1",
                "1.2.3-SNAPSHOT", "1.2.3-preview.1")) {
            Path dir = Files.createDirectory(temporary.resolve(version));
            String artifact = "WorldSafe-" + version + ".jar";
            byte[] bytes = "verified build".getBytes(StandardCharsets.UTF_8);
            Files.write(dir.resolve(artifact), bytes);
            String digest = digest(bytes);
            Files.write(dir.resolve(artifact + ".sha256"), (digest + "  " + artifact + "\n").getBytes(StandardCharsets.UTF_8));
            assertEquals(0, runRelease(shell, version, dir, digest), shell + " / " + version + ":\n"
                    + new String(Files.readAllBytes(dir.resolve("process.log")), StandardCharsets.UTF_8));
            List<String> args = Files.readAllLines(dir.resolve("gh-args"), StandardCharsets.UTF_8);
            assertEquals(Arrays.asList("release", "create", "v" + version, artifact, artifact + ".sha256"), args.subList(0, 5));
            assertEquals("0123456789abcdef0123456789abcdef01234567", args.get(args.indexOf("--target") + 1));
            boolean prerelease = !version.equals("1.2.3");
            assertEquals(prerelease, args.contains("--prerelease"));
            assertEquals(prerelease, args.contains("--latest=false"));
            assertFalse(args.contains("--latest"));

            Files.delete(dir.resolve("gh-args"));
            Files.write(dir.resolve(artifact), "tampered".getBytes(StandardCharsets.UTF_8));
            assertNotEquals(0, runRelease(shell, version, dir, digest));
            assertFalse(Files.exists(dir.resolve("gh-args")));
            // Even a modified checksum sidecar cannot override the digest supplied by the build job.
            String changed = digest("tampered".getBytes(StandardCharsets.UTF_8));
            Files.write(dir.resolve(artifact + ".sha256"), (changed + "  " + artifact + "\n").getBytes(StandardCharsets.UTF_8));
            assertNotEquals(0, runRelease(shell, version, dir, digest));
            assertFalse(Files.exists(dir.resolve("gh-args")));
            Files.delete(dir.resolve(artifact + ".sha256"));
            assertNotEquals(0, runRelease(shell, version, dir, digest));
            assertFalse(Files.exists(dir.resolve("gh-args")));
        }
    }

    @Test
    void releaseRequiresSameCommitCompatibilityAndVerifiedBuild() throws Exception {
        Map<?, ?> workflow = yaml(".github/workflows/build-and-release.yml");
        Map<?, ?> jobs = map(workflow.get("jobs"));
        Map<?, ?> release = map(jobs.get("release"));
        assertEquals(Arrays.asList("detect-version", "build", "compatibility"), release.get("needs"));
        // No always()/failure() bypass: GitHub's implicit success() gates failed or skipped prerequisites.
        assertEquals("needs.detect-version.outputs.should_release == 'true'", release.get("if"));
        Map<?, ?> compatibility = map(jobs.get("compatibility"));
        assertEquals("./.github/workflows/compatibility-matrix.yml", compatibility.get("uses"));
        assertFalse(compatibility.containsKey("continue-on-error"));
        Map<?, ?> matrix = yaml(".github/workflows/compatibility-matrix.yml");
        // SnakeYAML's YAML 1.1 resolver parses the GitHub key 'on' as Boolean.TRUE.
        Map<?, ?> triggers = map(matrix.containsKey("on") ? matrix.get("on") : matrix.get(Boolean.TRUE));
        assertTrue(triggers.containsKey("workflow_call"));
        for (Object job : map(matrix.get("jobs")).values()) {
            Map<?, ?> required = map(job);
            assertFalse(required.containsKey("continue-on-error"));
            for (Object step : (List<?>) required.get("steps")) {
                Map<?, ?> check = map(step);
                assertFalse(check.containsKey("continue-on-error"));
                if (String.valueOf(check.get("uses")).startsWith("actions/checkout@")) {
                    assertFalse(check.containsKey("with"), "Default checkout must use the caller commit");
                }
            }
        }
        Map<?, ?> build = map(jobs.get("build"));
        assertEquals("${{ steps.digest.outputs.sha256 }}", map(build.get("outputs")).get("sha256"));
        List<?> steps = (List<?>) build.get("steps");
        int verify = -1;
        int digest = -1;
        int upload = -1;
        for (int i = 0; i < steps.size(); i++) {
            Map<?, ?> step = map(steps.get(i));
            assertFalse(step.containsKey("continue-on-error"));
            assertFalse(step.containsKey("if"));
            if ("Verify release JAR".equals(step.get("name"))) verify = i;
            if ("digest".equals(step.get("id"))) digest = i;
            if (String.valueOf(step.get("uses")).startsWith("actions/upload-artifact@")) upload = i;
        }
        assertTrue(verify >= 0 && digest > verify && upload > digest);
        assertEquals("error", map(map(steps.get(upload)).get("with")).get("if-no-files-found"));
        assertTrue(String.valueOf(map(map(steps.get(upload)).get("with")).get("path")).contains(".sha256"));
        List<?> releaseSteps = (List<?>) release.get("steps");
        Map<?, ?> create = map(releaseSteps.get(releaseSteps.size() - 1));
        assertEquals("${{ needs.build.outputs.sha256 }}", map(create.get("env")).get("EXPECTED_SHA256"));
        assertTrue(String.valueOf(create.get("run")).contains("bash scripts/create-release.sh"));
    }

    private int runRelease(String version, Path directory, String digest) throws Exception {
        return runRelease("bash", version, directory, digest);
    }

    private int runRelease(String shell, String version, Path directory, String digest) throws Exception {
        Path bin = Files.createDirectories(directory.resolve("bin"));
        Path gh = bin.resolve("gh");
        Files.write(gh, "#!/usr/bin/env bash\nprintf '%s\\n' \"$@\" > \"$GH_ARGUMENT_LOG\"\n".getBytes(StandardCharsets.UTF_8));
        assertTrue(gh.toFile().setExecutable(true));
        ProcessBuilder builder = new ProcessBuilder(shell, Paths.get("scripts/create-release.sh").toAbsolutePath().toString(),
                version, directory.toString(), digest);
        builder.environment().put("PATH", bin + java.io.File.pathSeparator + System.getenv("PATH"));
        builder.environment().put("GH_ARGUMENT_LOG", directory.resolve("gh-args").toString());
        builder.environment().put("GITHUB_SHA", "0123456789abcdef0123456789abcdef01234567");
        builder.environment().remove("GH_TOKEN");
        builder.environment().remove("GITHUB_TOKEN");
        builder.redirectErrorStream(true).redirectOutput(directory.resolve("process.log").toFile());
        Process process = builder.start();
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Release script timed out");
        }
        return process.exitValue();
    }

    private String digest(byte[] bytes) throws Exception {
        StringBuilder hex = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            hex.append(String.format("%02x", value & 0xff));
        }
        return hex.toString();
    }

    private Map<?, ?> yaml(String path) throws Exception {
        try (InputStream input = Files.newInputStream(Paths.get(path))) {
            return map(new Yaml().load(input));
        }
    }

    private Map<?, ?> map(Object value) {
        return (Map<?, ?>) value;
    }
}
