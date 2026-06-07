package thisissadeghi.kickoff.data.remote.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import thisissadeghi.kickoff.data.token.TokenManager

/**
 * Ktor plugin that automatically adds authentication headers to requests
 * based on RequestConfig settings
 */
class TokenHeaderPlugin private constructor(
    private val tokenManager: TokenManager,
) {
    companion object Plugin : HttpClientPlugin<Plugin.Config, TokenHeaderPlugin> {
        override val key = AttributeKey<TokenHeaderPlugin>("TokenHeaderPlugin")

        class Config {
            var tokenManager: TokenManager? = null
        }

        override fun prepare(block: Config.() -> Unit): TokenHeaderPlugin {
            val config = Config().apply(block)
            return TokenHeaderPlugin(
                tokenManager =
                    config.tokenManager
                        ?: error("TokenManager must be provided to TokenHeaderPlugin"),
            )
        }

        override fun install(
            plugin: TokenHeaderPlugin,
            scope: HttpClient,
        ) {
            scope.plugin(HttpSend).intercept { request ->
                val configs = request.attributes.getOrNull(RequestConfigKey)

                // Add user auth token if required
                if (configs?.isUserAuthRequired() == true) {
                    runBlocking {
                        plugin.tokenManager.getAccessToken()?.let { token ->
                            if (token.isNotEmpty()) {
                                request.headers.append("Authorization", "Bearer $token")
                            }
                        }
                    }
                }

                execute(request)
            }
        }
    }
}
