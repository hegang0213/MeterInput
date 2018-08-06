package com.bdwater.meterinput;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.base.IReturnValueListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment {

    CApplication mApp;
    CurrentContext mCC;
    BookAdapter mAdapter;
    ListView listView;

    Integer mCheckPosition = -1;
    IReturnValueListener mListener;
    public BookListFragment() {
        // Required empty public constructor
    }

    public void setListener(IReturnValueListener l) {
        mListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        mApp = (CApplication)getActivity().getApplication();
        mCC = mApp.getCurrentContext();

        mCheckPosition = mCC.getBookPosition(mCC.getCurrentBook());

        listView = (ListView)view.findViewById(R.id.listView);
        mAdapter = new BookAdapter(view.getContext(), android.R.layout.simple_list_item_1, mCC.getBooks());
        listView.setAdapter(mAdapter);
        if(mCheckPosition >= 0)
            listView.setSelection(mCheckPosition);

        listView.setOnItemClickListener(listener);

        return view;
    }

    ListView.OnItemClickListener listener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            listView.setItemChecked(position, true);
            mCheckPosition = position;
            mAdapter.notifyDataSetChanged();
            if(mListener != null) {
                mListener.onReturnValue(mAdapter.getItem(position));
            }
        }
    };

    public void syncCheckState() {
        mCheckPosition = mCC.getBookPosition(mCC.getCurrentBook());
        if(mCheckPosition >= 0)
            listView.setSelection(mCheckPosition);
        mAdapter.notifyDataSetChanged();
    }

    class BookAdapter extends ArrayAdapter<Book> {

        public BookAdapter(Context context, int resource, Book[] books) {
            super(context, resource, books);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) super.getView(position, convertView, parent);
            textView.setTextSize(14);

            if(position == mCheckPosition) {
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                textView.setBackgroundResource(R.color.itemClickablePressedBackground);
            }
            else {
                textView.setBackgroundResource(android.R.color.transparent);
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            }

            return textView;
        }
    }

}
