# Atlas Kotlin SDK

## Installation

To utilize Atlas Kotlin SDK, copy **atlas-sdk-aar** (or download it from [https://github.com/atlas-support-inc/kotlin-sdk/raw/main/atlas-kotlin-sdk.zip](https://github.com/atlas-support-inc/kotlin-sdk/raw/main/atlas-kotlin-sdk.zip) into your project and adjust your settings accordingly:

```kts
// In build.gradle.kts. Check fo latest version
dependencies {
    implementation("so.atlas:atlas-sdk:1.5.1") 
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

        atlasSdk.init(this@AtlasDemoApplication, appId = "APP_ID")
    }
}
```

Retrieve your **APP_ID** from the [Organization Settings page](https://app.atlas.so/settings/company) in the Atlas application.

## Identification

To bind Atlas tickets to your user, confidently execute the identify method by inputting the user ID as the primary argument and the user hash if authentication is activated on the Installation Config page at https://app.atlas.so/configuration/installation. Alternatively, use an empty string if authentication is not enabled.

```kt
CoroutineScope(Dispatchers.IO).launch {
    AtlasSdk.identify(userId = "...", userHash = "...", name = "...", email = "...")
}
```

For logging out the user, simply call the `logout` method

```kt
atlasSdk.logout()
```

### For Java Developers

In addition to the standard Kotlin implementation, we provide an `identifyAsync` method specifically designed for Java environments. This method returns a `CompletableFuture` for handling asynchronous operations seamlessly.

#### Example in Java:  
```java
AtlasSdk.identifyAsync("USER_ID", "USER_HASH" /* or null */, Map.of(
    "name", "Jon Doe",
    "email", "jon@doe.com",
    "phoneNumber", "+1234567890"
))
    .thenRun(() -> System.out.println("User identified successfully"))
    .exceptionally(e -> {
        e.printStackTrace();
        return null;
    });
```

To log out the user in Java, call the `logoutAsync` method:
```java
AtlasSdk.logoutAsync()
        .thenRun(() -> System.out.println("User logged out successfully"));
```

## Atlas UI

**AtlasSDK** now provides an `AtlasFragment` for seamless integration into your app. You can navigate to `AtlasFragment` from any `Activity` or use a `FragmentContainerView` within your `Fragment` to embed it.

#### Example Setup in XML  
To include a `FragmentContainerView` in your layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/black"
    tools:context=".ui.notifications.NotificationsFragment" >

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragment_container_view"
        android:name="com.example.atlaskotlindemo.ui.notifications.NotificationsFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Example Integration in Kotlin  
Follow these steps to add and display the `AtlasFragment` dynamically:  

1. **Create the `AtlasFragment`:**  
   ```kotlin
   val atlasFragment = AtlasSdk.getAtlasFragment()
   ```

2. **Replace the Current Fragment with `AtlasFragment`:**  
   ```kotlin
   childFragmentManager.beginTransaction()
       .replace(binding.fragmentContainerView.id, atlasFragment)
       .commitNow()
   ```

This allows you to replace the existing `Fragment` in your app with the `AtlasFragment` in fullscreen or within a designated container.

#### Additional query parameters
`query` (String)

An optional `query` parameter in string format. The `query` is used to configure the behavior or content of the returned AtlasFragment.
- Default value: "" (empty string).
- Expected format: "key1: value1; key2: value2; ...."

```kotlin
    val atlasFragment = AtlasSdk.getAtlasFragment(query = "chatbotKey: report_bug; prefer: last")
   ```

`chatbotKey: KEY`: Specifies the chatbot that has to be started immediately when AtlasFragment is loaded

`prefer: last`: Instead of starting new chatbot everytime it will open the last not completed chatbot if exists

#### Additionally, you can monitor events occurring within the Atlas view:

```kt
val atlasSdk = (requireActivity().application as AtlasDemoApplication).atlasSdk

atlasSdk.addAtlasMessageHandler(
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
val atlasSdk = (requireActivity().application as AtlasDemoApplication).atlasSdk

// Or use as static object
// val atlasSdk = AtlasSDK 

atlasSdk.atlasStatsLive.observe(viewLifecycleOwner, { stats ->
    val count = stats?.conversations?.map { it.unread }?.sum() ?: 0
    binding.counterMessage.text = if (count == 0) {
        "You have no messages"
    } else {
        "You have $count unread messages"
    }
})
```
