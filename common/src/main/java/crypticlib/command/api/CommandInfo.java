package crypticlib.command.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandInfo {

    private String name;
    private String permission;
    private String[] aliases;
    private String description;
    private String usage;

    public CommandInfo(BukkitCommand commandAnnotation) {
        this(commandAnnotation.name(), commandAnnotation.permission().isEmpty() ? null : commandAnnotation.permission(), commandAnnotation.aliases(), commandAnnotation.description(), commandAnnotation.usage());
    }

    public CommandInfo(String name) {
        this(name, (String) null);
    }

    public CommandInfo(String name, String permission) {
        this(name, permission, new String[0]);
    }

    public CommandInfo(String name, String permission, String[] aliases) {
        this(name, permission, aliases, "");
    }

    public CommandInfo(String name, String permission, String[] aliases, String description) {
        this(name, permission, aliases, description, "");
    }

    public CommandInfo(String name, String permission, String[] aliases, String description, String usage) {
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
    }

    @NotNull
    public String name() {
        return name;
    }

    public CommandInfo setName(String name) {
        this.name = name;
        return this;
    }

    @Nullable
    public String permission() {
        return permission;
    }

    public CommandInfo setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    @NotNull
    public String[] aliases() {
        return aliases;
    }

    public CommandInfo setAliases(String[] aliases) {
        this.aliases = aliases;
        return this;
    }

    @NotNull
    public String description() {
        return description;
    }

    public CommandInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    @NotNull
    public String usage() {
        return usage;
    }

    public CommandInfo setUsage(String usage) {
        this.usage = usage;
        return this;
    }
}
