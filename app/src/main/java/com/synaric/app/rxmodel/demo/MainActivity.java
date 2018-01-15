package com.synaric.app.rxmodel.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.synaric.app.rxmodel.DbModel;
import com.synaric.app.rxmodel.RxModel;
import com.synaric.app.rxmodel.filter.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    /**
     * 全局对象，仅需创建一次。
     */
    private RxModel rxModel = new RxModel.Builder(this.getApplicationContext()).dbName("demo").build();

    /**
     * 创建操作特定数据的model。默认GameBean作为表名。可以创建多个model操作同一张表。
     */
    private DbModel<GameBean> model = new DbModel<GameBean>(rxModel) {
        @Override
        public String bindID(GameBean gameBean) {
            //指定主键
            return gameBean.getId();
        }
    };

    private List<GameBean> data = new ArrayList<>();
    private BaseAdapter adapter;
    private Random random = new Random();
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lvData = (ListView) findViewById(R.id.list);
        lvData.setAdapter(adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public Object getItem(int position) {
                return data.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText((String) getItem(position));
                ViewGroup.LayoutParams lp = textView.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                return textView;
            }
        });
    }

    private void save() {
        GameBean game1 = new GameBean(count++ + "", "game1", random.nextInt(100000));
        model.save(game1).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean result) {
                showToast("result:" + result);
                queryAll();
            }
        });
    }

    private void delete() {
        model.delete(new Filter<GameBean>() {
            @Override
            public boolean doIterativeFilter(GameBean gameBean) {
                return gameBean.getSize() < 20000 || gameBean.getSize() > 40000;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer count) {
                showToast("delete items:" + count);
                queryAll();
            }
        });
    }

    private void update() {
        GameBean game1 = new GameBean("1", "game1", 60000);
        model.save(game1).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean result) {
                showToast("result:" + result);
                queryAll();
            }
        });
    }

    private void queryAll() {
        model.queryAll().subscribe(new Action1<List<GameBean>>() {
            @Override
            public void call(List<GameBean> data) {
                MainActivity.this.data = data;
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }
}
