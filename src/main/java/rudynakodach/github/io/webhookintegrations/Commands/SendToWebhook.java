package rudynakodach.github.io.webhookintegrations.Commands;

import okhttp3.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import rudynakodach.github.io.webhookintegrations.PlayerEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SendToWebhook implements CommandExecutor {

    final PlayerEventListener playerEventListener;
    final FileConfiguration config;
    final JavaPlugin javaPlugin;

    final Logger logger;

    public SendToWebhook(PlayerEventListener _pel, FileConfiguration _cfg, JavaPlugin _javaPlugin, Logger _logger) {
        playerEventListener = _pel;
        config = _cfg;
        javaPlugin = _javaPlugin;
        logger = _logger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("send")) {
            if (args.length >= 2) {
                String webhookUrl = Objects.requireNonNull(config.getString("webhookUrl"));

                if (webhookUrl.equals("")) {

                    String response = ChatColor.translateAlternateColorCodes('&', "&cTwój webhook url jest &lPUSTY\n" +
                            "&eProszę użyć /seturl <url>");
                    sender.sendMessage(response);

                    return true;
                }

                boolean isEmbed = Boolean.parseBoolean(args[0]);
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                Player player = (Player) sender;
                String username = player.getName();

                logger.log(Level.INFO, player + " użyj /send z tą wiadmościa: " + message);

                if (isEmbed) {

                    int embedColor = config.getInt("onCommandForceMessageEmbedColor");

                    String json = "{" +
                            "\"embeds\": [" +
                            "{" +
                            "\"color\": " + embedColor + "," +
                            "\"title\": \"" + username + "\"," +
                            "\"description\": \"" + message + "\"" +
                            "}" +
                            "]" +
                            "}";

                    Send(json);

                } else {
                    message = username + ": " + message;

                    String json = "\"content\": \"" + message + "\"";

                    Send(json);
                }
                return true;

            } else {
                String response = ChatColor.translateAlternateColorCodes('&', "&cZłe użycie!\n" +
                        "&r&f/send &aisEmbed&7(true/false)&r&a wiadomość");
                sender.sendMessage(response);

                return true;
            }
        }
        return false;
    }

    public void Send(String json) {


        String webhookUrl = Objects.requireNonNull(config.getString("webhookUrl"));

        if (webhookUrl.equals("")) {
            logger.log(Level.WARNING, "Próba wysłania wiadomości do webhooka, ale webhook url jest pusty!");
            return;
        }

        new BukkitRunnable() {
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.get("application/json");
                RequestBody body = RequestBody.create(json, mediaType);
                Request request = new Request.Builder()
                        .url(webhookUrl)
                        .post(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        logger.log(Level.WARNING, "Wysyłanie wiadomości do webhooka nie powiodło się!" + response.body().toString());
                    }
                    response.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }

            }
        }.runTaskAsynchronously(javaPlugin);
    }

}
