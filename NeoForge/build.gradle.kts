import com.hypherionmc.modpublisher.properties.ModLoader

plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentNeoForge").extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

    runs.create("datagen") {
        data()
        programArgs("--all", "--mod", "woodwevegot")
        programArgs("--output", project(":Common").file("src/main/generated/resources").absolutePath)
        programArgs("--existing", project(":Common").file("src/main/resources").absolutePath)
        programArgs("--existing-mod", "biomeswevegone")
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${project.properties["neoforge_version"]}")

    "common"(project(":Common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":Common", "transformProductionNeoForge"))

    modLocalRuntime("me.djtheredstoner:DevAuth-neoforge:${project.properties["devauth_version"]}")

    modApi("net.potionstudios:Oh-The-Biomes-Weve-Gone-NeoForge:${project.properties["bwg_version"]}") { isTransitive = false }

    modRuntimeOnly("com.github.glitchfiend:TerraBlender-neoforge:$minecraftVersion-${project.properties["terrablender_version"]}")
    modRuntimeOnly("corgitaco.corgilib:Corgilib-NeoForge:$minecraftVersion-${project.properties["corgilib_version"]}")
    modRuntimeOnly("dev.corgitaco:Oh-The-Trees-Youll-Grow-neoforge:$minecraftVersion-${project.properties["ohthetreesyoullgrow_version"]}")
    modRuntimeOnly("software.bernie.geckolib:geckolib-neoforge-$minecraftVersion:${project.properties["geckolib_version"]}")

    modCompileOnly("mcp.mobius.waila:wthit-api:neo-${project.properties["WTHIT"]}")
    modLocalRuntime("mcp.mobius.waila:wthit:neo-${project.properties["WTHIT"]}")
    modLocalRuntime("lol.bai:badpackets:neo-${project.properties["badPackets"]}")

    modLocalRuntime("com.github.Jab125.architectury-data-generation-fix:architectury-data-generation-fix-neoforge:21.0.3")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("net/potionstudios/woodwevegot/neoforge/datagen/**", "architectury.common.json", ".cache/**")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
        atAccessWideners.add("woodwevegot.accesswidener")
    }
}

publisher {
    setLoaders(ModLoader.NEOFORGE)
}
