package com.gabrielbarros.siblingordating.data

/**
 * Represents a photo entry in the game.
 */
data class PhotoEntry(
    val id: Int,
    val label: String,           // "Couple 1", "Siblings 1", etc.
    val relationship: String,    // "dating" or "siblings"
    val imageUri: String?,       // null for built-in placeholders, file URI for uploads
    val isBuiltIn: Boolean = true
)

/**
 * Provides sample data and manages uploaded photos.
 */
object PhotoRepository {
    private val builtInPhotos = listOf(
        PhotoEntry(1, "Couple 1", "dating", null, true),
        PhotoEntry(2, "Siblings 1", "siblings", null, true),
        PhotoEntry(3, "Couple 2", "dating", null, true),
        PhotoEntry(4, "Siblings 2", "siblings", null, true),
        PhotoEntry(5, "Couple 3", "dating", null, true),
        PhotoEntry(6, "Siblings 3", "siblings", null, true),
        PhotoEntry(7, "Couple 4", "dating", null, true),
        PhotoEntry(8, "Siblings 4", "siblings", null, true),
        PhotoEntry(9, "Couple 5", "dating", null, true),
        PhotoEntry(10, "Siblings 5", "siblings", null, true),
    )

    private val uploadedPhotos = mutableListOf<PhotoEntry>()
    private var nextId = 100

    fun getAllPhotos(): List<PhotoEntry> = builtInPhotos + uploadedPhotos

    fun addPhoto(label: String, relationship: String, imageUri: String): PhotoEntry {
        val entry = PhotoEntry(nextId++, label, relationship, imageUri, false)
        uploadedPhotos.add(entry)
        return entry
    }

    fun getShuffled(): List<PhotoEntry> = getAllPhotos().shuffled()
}
