package jp.techacademy.yoshihiro.minagawa.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ym on 16/09/12.
 */
public class QuestionListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater = null;
    private ArrayList<Question> mQuestionsArrayList;

    public QuestionListAdapter(Context context){
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mQuestionsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mQuestionsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.list_questions, parent, false);
        }

        TextView titleText = (TextView)convertView.findViewById(R.id.titleTextView);
        titleText.setText(mQuestionsArrayList.get(position).getTitle());

        TextView nameText = (TextView)convertView.findViewById(R.id.nameTextView);
        nameText.setText(mQuestionsArrayList.get(position).getName());

        TextView resText = (TextView)convertView.findViewById(R.id.resTextView);
        int resNum = mQuestionsArrayList.get(position).getAnswers().size();
        resText.setText(String.valueOf(resNum));

        byte[] bytes = mQuestionsArrayList.get(position).getImageBytes();
        if(bytes.length != 0){
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
            ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }

        return convertView;
    }

    public void setQuestionsArrayList(ArrayList<Question> questionsArrayList){
        mQuestionsArrayList = questionsArrayList;
    }
}
