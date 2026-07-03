rootProject.name = "CrypticLib"

include("platform:common", "platform:bukkit", "platform:bungee", "platform:velocity")
include(
    "module:bukkit:ui",
    "module:bukkit:conversation",
    "module:bukkit:i18n",
    "module:bukkit:particle",
)
include(
    "module:common:database",
    "module:common:resource",
    "module:common:compat",
    "module:common:script"
)
