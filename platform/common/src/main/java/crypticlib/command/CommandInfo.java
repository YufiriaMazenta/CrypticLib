package crypticlib.command;

import crypticlib.perm.PermInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandInfo {

    @NotNull
    private String name;
    @Nullable
    private PermInfo permission;
    @NotNull
    private final List<String> aliases = new ArrayList<>();
    @Nullable
    private String description;
    @Nullable
    private String usage;

    public CommandInfo(@NotNull String name) {
        this(name, (PermInfo) null);
    }

    public CommandInfo(@NotNull String name, @Nullable PermInfo permission) {
        this(name, permission, new ArrayList<>());
    }

    public CommandInfo(@NotNull String name, @Nullable List<String> aliases) {
        this(name, null, aliases);
    }

    public CommandInfo(@NotNull String name, @Nullable PermInfo permission, @Nullable List<String> aliases) {
        this(name, permission, aliases, "");
    }

    public CommandInfo(@NotNull String name, @Nullable PermInfo permission, @Nullable List<String> aliases, @Nullable String description) {
        this(name, permission, aliases, description, "");
    }

    public CommandInfo(@NotNull String name, @Nullable PermInfo permission, @Nullable List<String> aliases, @Nullable String description, @Nullable String usage) {
        this.name = name;
        this.permission = permission;
        this.aliases.addAll(aliases != null ? aliases : new ArrayList<>());
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
    public PermInfo permission() {
        return permission;
    }

    public CommandInfo setPermission(@NotNull String permission) {
        this.permission = new PermInfo(permission);
        return this;
    }

    public CommandInfo setPermission(@Nullable PermInfo permission) {
        this.permission = permission;
        return this;
    }

    @NotNull
    public List<String> aliases() {
        return aliases;
    }

    public CommandInfo setAliases(@NotNull List<String> aliases) {
        this.aliases.clear();
        this.aliases.addAll(aliases);
        return this;
    }

    @Nullable
    public String description() {
        return description;
    }

    public CommandInfo setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    @Nullable
    public String usage() {
        return usage;
    }

    public CommandInfo setUsage(@NotNull String usage) {
        this.usage = usage;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private String name;
        private PermInfo permission;
        private List<String> aliases;
        private String description;
        private String usage;

        public Builder() {}

        public Builder(String name) {
            this.name = name;
        }

        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder permission(PermInfo permission) {
            this.permission = permission;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder aliases(@Nullable List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public CommandInfo build() {
            Objects.requireNonNull(name);
            return new CommandInfo(name, permission, aliases, description, usage);
        }

    }

}
