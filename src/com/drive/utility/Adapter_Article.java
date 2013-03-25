package com.drive.utility;

import java.util.ArrayList;
import java.util.List;


import com.drive.utility.database.Article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Adapter_Article extends BaseAdapter{
	
	private List<Article> items;
	private LayoutInflater inflater;
	
	public Adapter_Article(Context context, List<Article> items) {
		this.items = items;
		this.inflater = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void addArticle(Article article){
		items.add(article);
	}
	
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final Article item = (Article) getItem(position);
		
		ViewHolder holder;
		if (convertView == null) {  
			
			convertView = inflater.inflate(R.layout.main_row, null);

			holder = new ViewHolder();      
			
			holder.title = (TextView) convertView.findViewById(R.id.main_row_title);   
			holder.summ = (TextView) convertView.findViewById(R.id.main_row_summ);   
			
			
			convertView.setTag(holder);            
		} else {  
		    holder = (ViewHolder) convertView.getTag();
		}
		
		
		holder.title.setText(String.valueOf(item.getTitle() ));
		holder.summ.setText(String.valueOf( item.getSumm() ));
        
        return convertView;
	}
	
	
	static class ViewHolder {   
	    TextView title; 
	    TextView summ;
	}


	public void deleteAllArticles() {
		items.removeAll(items);
	}

	public void setArticles(ArrayList<Article> items) {
		this.items = items;
	} 

}
