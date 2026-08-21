{
  description = "Portal Cornerlink - Minecraft 26.2 Fabric Mod";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        # Minecraft 26.x requires Java 25.
        jdk = pkgs.jdk25;
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk

            # Gradle wrapper will be used, but having gradle available is useful
            gradle

            # Development tools
            git
          ];

          shellHook = ''
            echo "Portal Cornerlink Development Environment"
            echo "Java version: $(java -version 2>&1 | head -n 1)"
            echo "Gradle version: $(gradle --version | grep Gradle | head -n 1)"
            echo ""
            echo "To build the mod: ./gradlew build"
            echo "To run the client: ./gradlew runClient"
            echo ""

            # Set JAVA_HOME for Gradle
            export JAVA_HOME="${jdk}"

            # Ensure gradlew is executable
            if [ -f ./gradlew ]; then
              chmod +x ./gradlew
            fi
          '';

          # Set environment variables for the shell
          JAVA_HOME = "${jdk}";

          # Optional: Set gradle properties for better performance on Linux
          GRADLE_OPTS = "-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true";
        };
      }
    );
}
