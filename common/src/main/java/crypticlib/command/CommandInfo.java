package crypticlib.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandInfo {

    @NotNull
    private String name;
    @Nullable
    private String permission;
    @NotNull
    private String[] aliases;
    @NotNull
    private String description;
    @NotNull
    private String usage;

    public CommandInfo(@NotNull BukkitCommand commandAnnotation) {
        this(commandAnnotation.name(), commandAnnotation.permission() == null || commandAnnotation.permission().isEmpty() ? null : commandAnnotation.permission(), commandAnnotation.aliases(), commandAnnotation.description(), commandAnnotation.usage());
    }

    public CommandInfo(@NotNull String name) {
        this(name, (String) null);
    }

    public CommandInfo(@NotNull String name, @Nullable String permission) {
        this(name, permission, new String[0]);
    }

    public CommandInfo(@NotNull String name, @NotNull String[] aliases) {
        this(name, null, aliases);
    }

    public CommandInfo(@NotNull String name, @Nullable String permission, @NotNull String[] aliases) {
        this(name, permission, aliases, "");
    }

    public CommandInfo(@NotNull String name, @Nullable String permission, @NotNull String[] aliases, @NotNull String description) {
        this(name, permission, aliases, description, "");
    }

    public CommandInfo(@NotNull String name, @Nullable String permission, @NotNull String[] aliases, @NotNull String description, @NotNull String usage) {
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

    public CommandInfo setName(@NotNull String name) {
        this.name = name;
        return this;
    }

    @Nullable
    public String permission() {
        return permission;
    }

    public CommandInfo setPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @NotNull
    public String[] aliases() {
        return aliases;
    }

    public CommandInfo setAliases(@NotNull String[] aliases) {
        this.aliases = aliases;
        return this;
    }

    @NotNull
    public String description() {
        return description;
    }

    public CommandInfo setDescription(@NotNull String description) {
        this.description = description;
        return this;
    }

    @NotNull
    public String usage() {
        return usage;
    }

    public CommandInfo setUsage(@NotNull String usage) {
        this.usage = usage;
        return this;
    }
}
