package list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mymusic.R;

import java.util.List;

public class SongAdapter extends ArrayAdapter {
    private int resourceId;

    public SongAdapter(Context context, int textViewResourceId, List<Song> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Song song = (Song) getItem(position);    //获取当前项的Song实例

        //提升ListView的运行效率：不会重复加载布局，对控件的实例进行缓存。
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.songId = (TextView) view.findViewById(R.id.song_id);
            viewHolder.songName = (TextView) view.findViewById(R.id.song_name);
            viewHolder.songAlbum = (TextView) view.findViewById(R.id.song_album);
            viewHolder.songAuthor = (TextView) view.findViewById(R.id.song_author);
            view.setTag(viewHolder);    //将ViewHolder存储在view中

        }else {
            view = convertView;
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.songId.setText(song.getId());
        viewHolder.songName.setText(song.getName());
        viewHolder.songAlbum.setText(song.getAblum());
        viewHolder.songAuthor.setText(song.getAuthor());
        return view;
    }

    class ViewHolder{
        TextView songId;
        TextView songAlbum;
        TextView songAuthor;
        TextView songName;
    }

}
