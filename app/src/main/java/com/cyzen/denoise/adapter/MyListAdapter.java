package com.cyzen.denoise.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.cyzen.denoise.Constants;
import com.cyzen.denoise.R;

import java.util.List;
import java.util.Map;

public class MyListAdapter extends BaseAdapter {

    private final List<Map<String, Object>> mData;
    private boolean hasImage = false, hasImageEnd = false, hasBool = false;

    private final int TYPE_CATEGORY = 0;
    private final int TYPE_CONTENT = 1;
    private final int TYPE_ALERT = 2;

    public MyListAdapter(List<Map<String, Object>> list, String[] keys) {
        this.mData = list;

        //Check
        if (keys[0].equals(Constants.img)) {
            hasImage = true;
        }

        if (keys[keys.length - 2].equals(Constants.imgEnd) || keys[keys.length - 1].equals(Constants.imgEnd)) {
            hasImageEnd = true;
        }

        if (keys[keys.length - 1].equals(Constants.bool)) {
            hasBool = true;
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).containsKey(Constants.category)) {
            return TYPE_CATEGORY;
        } else if (mData.get(position).containsKey(Constants.alert)) {
            return TYPE_ALERT;
        }

        return TYPE_CONTENT;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItemViewType(position) == TYPE_CATEGORY) {
            return false;
        } else if (getItemViewType(position) == TYPE_ALERT) {
            return false;
        }

        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int itemType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (itemType) {
                case TYPE_CATEGORY:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
                    holder.category = convertView.findViewById(R.id.category);
                    break;
                case TYPE_CONTENT:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                    if (hasImage) {
                        holder.image = convertView.findViewById(R.id.image);
                    }
                    holder.title = convertView.findViewById(R.id.title);
                    holder.value = convertView.findViewById(R.id.value);
                    if (hasImageEnd) {
                        holder.imageEnd = convertView.findViewById(R.id.image_end);
                    }
                    if (hasBool) {
                        holder.Switch = convertView.findViewById(R.id.Switch);
                    }
                    break;
                case TYPE_ALERT:
                default:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alert, parent, false);
                    holder.alert = convertView.findViewById(R.id.item_alert);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> map = mData.get(position);
        Object obj;

        switch (itemType) {
            case TYPE_CATEGORY:
                obj = map.get(Constants.category);
                if (obj instanceof Integer) {
                    holder.category.setText((Integer) obj);
                } else if (obj instanceof CharSequence) {
                    holder.category.setText((CharSequence) obj);
                }
                break;
            case TYPE_CONTENT:
                if (hasImage) {
                    obj = map.get(Constants.img);
                    if (obj instanceof Integer) {
                        holder.image.setBackgroundResource((Integer) obj);
                        holder.image.setVisibility(View.VISIBLE);
                    } else {
                        holder.image.setVisibility(View.GONE);
                    }
                }

                obj = map.get(Constants.key);
                if (obj == null) {
                    holder.title.setVisibility(View.GONE);
                } else {
                    holder.title.setVisibility(View.VISIBLE);
                    if (obj instanceof Integer) {
                        holder.title.setText((Integer) obj);
                    } else if (obj instanceof CharSequence) {
                        holder.title.setText((CharSequence) obj);
                    }
                }

                obj = map.get(Constants.val);
                if (obj == null) {
                    holder.value.setVisibility(View.GONE);
                } else {
                    holder.value.setVisibility(View.VISIBLE);
                    if (obj instanceof Integer) {
                        holder.value.setText((Integer) obj);
                    } else if (obj instanceof CharSequence) {
                        holder.value.setText((CharSequence) obj);
                    }
                }

                if (hasImageEnd) {
                    obj = map.get(Constants.imgEnd);
                    if (obj instanceof Integer) {
                        holder.imageEnd.setBackgroundResource((Integer) obj);
                        holder.imageEnd.setVisibility(View.VISIBLE);
                    } else {
                        holder.imageEnd.setVisibility(View.GONE);
                    }
                }

                if (hasBool) {
                    obj = map.get(Constants.bool);
                    if (obj instanceof Boolean) {
                        holder.Switch.setChecked((Boolean) obj);
                        holder.Switch.setVisibility(View.VISIBLE);
                    } else {
                        holder.Switch.setVisibility(View.GONE);
                    }
                }
                break;
            case TYPE_ALERT:
                obj = map.get(Constants.alert);
                if (obj instanceof Integer) {
                    holder.alert.setText((Integer) obj);
                } else if (obj instanceof CharSequence) {
                    holder.alert.setText((CharSequence) obj);
                }
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        TextView category;

        View image;
        TextView title, value, alert;
        View imageEnd;
        SwitchCompat Switch;
    }
}