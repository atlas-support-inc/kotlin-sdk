# Atlas Kotlin SDK

## Installation

To utilize Atlas Kotlin SDK, copy **atlas-sdk-aar** (or download it from [https://github.com/atlas-support-inc/kotlin-sdk/raw/main/atlas-kotlin-sdk.zip](https://github.com/atlas-support-inc/kotlin-sdk/raw/main/atlas-kotlin-sdk.zip)) into your project and adjust your settings accordingly:

```kts
// build.gradle.kts
dependencies {
    implementation(project(mapOf("path" to ":atlas-sdk-aar")))
}
```

```kts
// settings.gradle.kts

include(":atlas-sdk-aar")
```

Next, proceed to instantiate the SDK within your application:

```kt
import android.app.Application
import com.atlas.sdk.AtlasSdk

class YourApplication : Application() {

    val atlasSdk : AtlasSdk = AtlasSdk

    override fun onCreate() {
        super.onCreate()

        atlasSdk.init(this@AtlasDemoApplication, "APP_ID")
    }
}
```

Retrieve your **APP_ID** from the [Organization Settings page](https://app.atlas.so/settings/company) in the Atlas application.

## Identification

To bind Atlas tickets to your user, confidently execute the identify method by inputting the user ID as the primary argument and the user hash if authentication is activated on the Installation Config page at https://app.atlas.so/configuration/installation. Alternatively, use an empty string if authentication is not enabled.

```kt
atlasSdk.identify(userId = "...", userHash = "...", userName = "...", userEmail = "...")
```

For logging out the user, simply call the identify method with a null value:

```kt
atlasSdk.identify(null)
```

## Atlas UI

To display the Atlas UI, integrate it into your layout and bind the view to the SDK:

```xml
<com.atlas.sdk.view.AtlasView
    android:id="@+id/atlas_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kt
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Initialize Atlas SDK view
    (requireActivity().application as AtlasDemoApplication).atlasSdk.bindAtlasView(lifecycle, binding.atlasView)
    binding.atlasView.openPage()
}
```

Additionally, you can monitor events occurring within the Atlas view:

```kt
binding.atlasView.setAtlasMessageHandler(
    object : AtlasMessageHandler() {
        override fun onError(message: String?) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Log.d("AtlasView", "onError: $message")
            }
        }

        override fun onNewTicket(ticketId: String?) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Log.d("AtlasView", "onNewTicket: $ticketId")
                // (requireActivity().application as AtlasDemoApplication).atlasSdk.updateCustomFields(ticketId, mapOf("customField" to "customValue")
            }
        }

        override fun onChangeIdentity(
            atlasId: String?,
            userId: String?,
            userHash: String?
        ) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Log.d(
                    "AtlasView",
                    "onChangeIdentity: $atlasId $userId $userHash"
                )
            }
        }
    }
)
```

## Subscribe for new messages

The following callback will provide the count of unread messages for each conversation. In this example, the total number of unread messages is output to the console:

```kt
(requireActivity().application as AtlasDemoApplication).atlasSdk.atlasStatsLive.observe(viewLifecycleOwner, { stats ->
    val count = stats?.conversations?.map { it.unread }?.sum() ?: 0
    binding.counterMessage.text = if (count == 0) {
        "You have no messages"
    } else {
        "You have $count unread messages"
    }
})
```
