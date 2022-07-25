package io.github.KismetNetwork;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

@Plugin(id = "luckpermsgroupwhitelist", name = "Luckperms Group Whitelist", version = "0.1.0-SNAPSHOT",
        url = "https://github.com/KismetNetwork/Luckperms-Group-Whitelist", description = "Only allow a certain luckperms group to join a server ", authors = {"AI-nsley69"})
public class LuckpermsGroupWhitelist {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final File config;
    private static LuckPerms luckPerms = LuckPermsProvider.get();
    // Initialization
    @Inject
    public LuckpermsGroupWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
            this.server = server;
            this.logger = logger;
            this.dataDirectory = dataDirectory;

            // Initial stuff
            logger.info("Luckperms Group Whitelist for Velocity is loading!");
            // TODO: load config
            Path configPath = dataDirectory.resolve("config.toml");
            this.config = configPath.toFile();
    }
    // onServerPreConnect event to check if the player is in the group it needs to be able to connect
    @Subscribe(order = PostOrder.EARLY)
    public static void onServerPreConnect(ServerPreConnectEvent event) {
        // Get the player object
        Player player = event.getPlayer();
        // Get the server and then the name from the server
        RegisteredServer server = event.getOriginalServer();
        String serverName = server.getServerInfo().getName();
        // Get the luckperms user & group
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        Set<String> groups = user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
        // TODO: check if server is in config & player is allowed
        /*
        Component kickMessage = Component.text("You are not authorized to connect to this server!");
        player.disconnect(kickMessage);
        */
    }
}
