package dev.kbzon.launcher.search

interface Searchable {
    val score: ResultScore
        get() = ResultScore.Unspecified
}