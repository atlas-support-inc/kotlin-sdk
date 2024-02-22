# Atlas Kotlin SDK

## Installation

To utilize Atlas Kotlin SDK, simply copy **atlas-sdk-aar** into your project and adjust your settings accordingly:

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
atlasSdk.identify(AtlasUser("USER_ID", ""))
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
private val binding get() = _binding!!

// Variables required for file selection
private var uploadFileCallback: ValueCallback<Array<Uri>>? = null
private val FILE_CHOOSER_REQUEST_CODE = 1

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Bind the view to SDK
    (requireActivity().application as YourApplication).atlasSdk.bindAtlasView(binding.atlasView)
    binding.atlasView.openPage()

    // Allow Atlas UI to select files to attach to the messages
    binding.atlasView.webChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            // Create an Intent for choosing files from the filesystem, including photos and videos.
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            // Create an Intent for capturing images.
            val capturePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create an Intent for capturing video.
            val captureVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val intentList: MutableList<Intent> = ArrayList()
            intentList.add(capturePhotoIntent)
            intentList.add(captureVideoIntent)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())

            // Start the activity to select a file, capture a photo, or record a video.
            startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)

            // Keep a reference to the ValueCallback which will receive the result.
            uploadFileCallback = filePathCallback

            return true
        }
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // Handle file selection
    if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
        if (null == uploadFileCallback || resultCode != RESULT_OK || data == null) {
            uploadFileCallback?.onReceiveValue(null)
            uploadFileCallback = null
            return
        }

        // Handle the case where the user captures a photo or video or selects one from the gallery.
        val result = if (data.data != null) arrayOf(data.data!!) else WebChromeClient.FileChooserParams.parseResult(resultCode, data)
        uploadFileCallback?.onReceiveValue(result)
        uploadFileCallback = null
    }
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
(requireActivity().application as YourApplication).atlasSdk.atlasStatsLive.observe(viewLifecycleOwner, { stats ->
    val count = stats?.conversations?.map { it.unread }?.sum() ?: 0
    if (count == 0) {
        Log.d("Atlas", "You have no messages")
    } else {
        Log.d("Atlas", "You have $count unread messages")
    }
})
```
