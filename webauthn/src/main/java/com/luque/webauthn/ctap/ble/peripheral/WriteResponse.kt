package com.luque.webauthn.ctap.ble.peripheral

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import com.luque.webauthn.ctap.ble.peripheral.WriteRequest

class WriteResponse(
    private val req: WriteRequest
) {

    var status: Int = BluetoothGatt.GATT_SUCCESS

    internal fun finishOn(server: BluetoothGattServer) {
        if (req.responseNeeded) {
            val offset = if (status == BluetoothGatt.GATT_FAILURE) {
                0
            } else {
                req.offset
            }
            server.sendResponse(req.device, req.requestId, status, offset, null)
        }
    }

}