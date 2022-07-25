package io.github.KismetNetwork;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Plugin(id = "luckpermsgroupwhitelist", name = "Luckperms Group Whitelist", version = "0.1.0-SNAPSHOT",
        url = "https://github.com/KismetNetwork/Luckperms-Group-Whitelist", description = "Only allow a certain luckperms group to join a server ", authors = {"AI-nsley69"})
public class LuckpermsGroupWhitelist {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final HashMap<String, List<String>> allowedGroupLookup = new HashMap<>();

    // Initialization
    @Inject
    public LuckpermsGroupWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws IOException, IllegalArgumentException {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        Path config = dataDirectory.resolve("config.toml");

        if (Files.notExists(config)) {
            try (InputStream input = LuckpermsGroupWhitelist.class.getResourceAsStream("/assets/lpgw/default.toml")) {
                Files.createDirectories(dataDirectory);
                // Files.createFile(config);
                Files.copy(Objects.requireNonNull(input, "Jar or class loader is bad."), config);
            }
        } else try (InputStream input = Files.newInputStream(config)) {
            Toml toml = new Toml();
            toml.read(input);

            Toml servers = toml.getTable("servers");
            for (var entry : servers.entrySet()) {
                Object entryValue = entry.getValue();
                if (entryValue instanceof Collection<?>) {
                    var values = new ArrayList<String>();

                    for (var value : (Collection<?>) entryValue) {
                        if (value instanceof String) {
                            values.add((String) value);
                        } else {
                            throw new IllegalArgumentException("Invalid entry for key " + entry.getKey() + ": " + value + "; contained within " + entryValue);
                        }
                    }

                    allowedGroupLookup.put(entry.getKey(), values);
                } else {
                    throw new IllegalArgumentException("Invalid object for key " + entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }
    // onServerPreConnect event to check if the player is in the group it needs to be able to connect
    @Subscribe(order = PostOrder.EARLY)
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // Get the server and then the name from the server
        RegisteredServer server = event.getOriginalServer();
        String serverName = server.getServerInfo().getName();

        // Check if requiredGroups has permissions, otherwise return
        List<String> requiredGroups = allowedGroupLookup.get(serverName);
        if (requiredGroups == null || requiredGroups.isEmpty()) return;

        // Get the player object
        Player player = event.getPlayer();

        // Get the luckperms user & group
        User user = LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(player);
        Set<String> groups = user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());

        // Iterate over the required groups and check if the user is in said group
        boolean isAllowed = false;
        for (String g : requiredGroups) {
            if (groups.contains(g)) {
                isAllowed = true;
                break;
            }
        }

        // Check if user is allowed, otherwise deny them
        if (isAllowed) return;
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
    }
}
