import re

with open("app/src/main/java/com/example/ui/screens/DictionarySearchScreen.kt", "r") as f:
    content = f.read()

# Replace UrbanItemCard to remove likes/dislikes
old_urban_card = """            // Author and Upvotes/Downvotes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${item.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvotes",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.thumbsUp}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Downvotes",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.thumbsDown}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }"""

new_urban_card = """            // Author
            Text(
                text = "by ${item.author}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )"""

content = content.replace(old_urban_card, new_urban_card)

with open("app/src/main/java/com/example/ui/screens/DictionarySearchScreen.kt", "w") as f:
    f.write(content)
