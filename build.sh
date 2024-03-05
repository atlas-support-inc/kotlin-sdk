#!/bin/bash

./gradlew build -Produle=atlas-sdk

zip -r atlas-kotlin-sdk.zip atlas-sdk-aar -x "*/build/*"