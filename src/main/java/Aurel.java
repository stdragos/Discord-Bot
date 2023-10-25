import io.github.cdimascio.dotenv.Dotenv;
import kotlin.random.Random;
import listeners.EventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;

public class Aurel{
    private final ShardManager shardManager;
    private final Dotenv config;
    public Aurel() throws LoginException{
        config = Dotenv.configure().ignoreIfMissing().load();



        String token = config.get("TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token,
                GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES);

        builder.setStatus(OnlineStatus.IDLE);
        builder.setActivity(Activity.playing("discord"));
        shardManager = builder.build();
        //builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES);
        //Register Listeners

        shardManager.addEventListener(new EventListener());
    }
    public Dotenv getConfig() {
        return this.config;
    }

    public static void main(String[] args)
    {
        try {
            Aurel bot = new Aurel();
        }
        catch (LoginException exception) {
            System.out.println("ERROR: Invalid token!");
        }
    }
}
