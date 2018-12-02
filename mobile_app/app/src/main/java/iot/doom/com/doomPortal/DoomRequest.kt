package iot.doom.com.doomPortal

data class AuthorizeRequest(val authorize: Boolean)

data class PairingRequest(val deviceId: String)

data class WhitelistRequest(val picture: String, val name: String)