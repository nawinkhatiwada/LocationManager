package com.androidbolts.library.permissions

internal class PermissionProvider private constructor(): PermissionManager() {

    companion object {
        private var  permissionProvider:PermissionProvider?=null
        fun getPermissionManager(): PermissionProvider {
            if (permissionProvider ==null){
                permissionProvider = PermissionProvider()
            }
            return permissionProvider!!
        }
    }

}