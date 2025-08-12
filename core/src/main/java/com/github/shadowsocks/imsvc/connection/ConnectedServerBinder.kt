package com.github.shadowsocks.imsvc.connection

import com.github.shadowsocks.aidl.imsvc.IConnectedServerService
import com.github.shadowsocks.imsvc.ImsvcService

class ConnectedServerBinder(private val service: ImsvcService) : IConnectedServerService.Stub() {
    override fun setConnectedTo(connectedTo: ConnectedTo?) {
        service.connectedTo.set(connectedTo)
    }

    override fun getConnectedTo(): ConnectedTo? = service.connectedTo.get()
}
