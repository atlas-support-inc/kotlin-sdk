#!/bin/bash

./gradlew build -Produle=atlas-sdk

cp atlas-sdk/build/outputs/aar/atlas-sdk-release.aar atlas-sdk-aar/atlas-sdk.aar
zip -r atlas-kotlin-sdk.zip atlas-sdk-aar -x "*/build/*"