package com.zx.zxtvsettings.bluetooth.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.telecom.Log;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zx.zxtvsettings.R;
import com.zx.zxtvsettings.Utils.Logger;
import com.zx.zxtvsettings.bluetooth.A2dpProfile;
import com.zx.zxtvsettings.bluetooth.CachedBluetoothDevice;
import com.zx.zxtvsettings.bluetooth.HeadsetProfile;
import com.zx.zxtvsettings.bluetooth.HidProfile;
import com.zx.zxtvsettings.bluetooth.LocalBluetoothProfile;
import com.zx.zxtvsettings.bluetooth.Utils;

import java.util.List;

/**
 * User: ShaudXiao
 * Date: 2017-05-09
 * Time: 11:21
 * Company: zx
 * Description:
 * FIXME
 */


public class BluetoothDeviceView extends FrameLayout implements
        CachedBluetoothDevice.Callback {

    private static int sDimAlpha = Integer.MIN_VALUE;

    private final CachedBluetoothDevice mCachedDevice;

    private OnClickListener mOnSettingsClickListener;

    private AlertDialog mDisconnectDialog;

    private TextView mDeviceName;
    private ImageView mDeviceIcon;
    private TextView mDeviceSumary;


    public BluetoothDeviceView(@NonNull Context context, CachedBluetoothDevice cachedDevice) {
        super(context);

        mCachedDevice = cachedDevice;

        View view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth, this);

        mDeviceName = (TextView) view.findViewById(R.id.item_bluetooth_name);
        mDeviceIcon = (ImageView) view.findViewById(R.id.item_bluetooth_iv);
        mDeviceSumary = (TextView) view.findViewById(R.id.item_bluetooth_pired);

        mCachedDevice.registerCallback(this);

        bindView();

        setBackgroundResource(R.drawable.sel_focus);
        setFocusable(true);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

    }


    private void bindView() {
        mDeviceName.setText(mCachedDevice.getName());
//        int summaryResId = getConnectionSummary();
        int summaryResId = Utils.getConnectionSummary(mCachedDevice);
        if (summaryResId != 0) {
            mDeviceSumary.setText(summaryResId);
        } else {
            mDeviceSumary.setText(null);   // empty summary for unpaired devices
        }

        int iconResId = getBtClassDrawable();
        if (iconResId != 0) {
            mDeviceIcon.setBackgroundResource(iconResId);
        }

        // Used to gray out the item
        setEnabled(!mCachedDevice.isBusy());

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCachedDevice.unregisterCallback(this);
    }


    @Override
    public void onDeviceAttributesChanged() {
        bindView();
    }

    public void onClicked() {
        int bondState = mCachedDevice.getBondState();

        if (mCachedDevice.isConnected()) {
            askDisconnect();
        } else if (bondState == BluetoothDevice.BOND_BONDED) {
            mCachedDevice.connect(true);
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            pair();
        }
    }

    private void askDisconnect() {
        Context context = getContext();
        String name = mCachedDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }
        String message = context.getString(R.string.bluetooth_disconnect_all_profiles, name);
        String title = context.getString(R.string.bluetooth_disconnect_title);

        DialogInterface.OnClickListener disconnectListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCachedDevice.disconnect();
            }
        };

        mDisconnectDialog = Utils.showDisconnectDialog(context,
                mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }

    private void pair() {
        if (!mCachedDevice.startPairing()) {
            Utils.showError(getContext(), mCachedDevice.getName(),
                    R.string.bluetooth_pairing_error_message);
        } else {


        }
    }

    private int getBtClassDrawable() {
        BluetoothClass btClass = mCachedDevice.getBtClass();
        if (btClass != null) {
            switch (btClass.getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.COMPUTER:
                    return R.drawable.ic_bt_laptop;

                case BluetoothClass.Device.Major.PHONE:
                    return R.drawable.ic_bt_cellphone;

                case BluetoothClass.Device.Major.PERIPHERAL:
                    return HidProfile.getHidClassDrawable(btClass);

                case BluetoothClass.Device.Major.IMAGING:
                    return R.drawable.ic_bt_imaging;

                default:
                    // unrecognized device class; continue
            }
        } else {
            Logger.getLogger().w( "mBtClass is null");
        }

        List<LocalBluetoothProfile> profiles = mCachedDevice.getProfiles();
        for (LocalBluetoothProfile profile : profiles) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return resId;
            }
        }
        if (btClass != null) {
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;

            }
            if (btClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        }
        return R.drawable.ic_settings_bluetooth2;
    }


//    public interface IDeviceStateChangeListener {
//
//        void onDeviceStateChange(CachedBluetoothDevice device);
//
//    }

}
