import re

with open("app/src/main/java/com/example/data/repository/AiRepository.kt", "r") as f:
    content = f.read()

# For reverseConceptSearch
old_call_1 = """            val response = openRouterApi.getChatCompletions(
                url = url,
                authorization = "Bearer $apiKey",
                request = request
            )"""
new_call_1 = """            val apiName = preferences.openRouterApiName.first()
            val response = openRouterApi.getChatCompletions(
                url = url,
                authorization = "Bearer $apiKey",
                title = apiName.ifBlank { "LexiVerse Android" },
                request = request
            )"""
content = content.replace(old_call_1, new_call_1)

# For testConnection
old_call_2 = """            val response = openRouterApi.getChatCompletions(
                url = url,
                authorization = "Bearer $apiKey",
                request = request
            )"""
new_call_2 = """            val apiName = preferences.openRouterApiName.first()
            val response = openRouterApi.getChatCompletions(
                url = url,
                authorization = "Bearer $apiKey",
                title = apiName.ifBlank { "LexiVerse Android" },
                request = request
            )"""
content = content.replace(old_call_2, new_call_2)

with open("app/src/main/java/com/example/data/repository/AiRepository.kt", "w") as f:
    f.write(content)

