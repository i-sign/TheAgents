package org.signin.theagents

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform