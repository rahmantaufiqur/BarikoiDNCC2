package xyz.barikoi.barikoidncc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AssessmentActivity extends AppCompatActivity {
    TextView floors,sqtperFloor,placement,type,category,pricepersqt,holdingTax,totalsqt;
    int floor,sqtpf,tsqt;
    String pm,t,c;
    Double ppsqft,ht;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
        init();

    }
    public void init(){
        floors=findViewById(R.id.floors);
        sqtperFloor=findViewById(R.id.sqftperfloor);
        placement=findViewById(R.id.placement);
        type=findViewById(R.id.type);
        category=findViewById(R.id.category);
        pricepersqt=findViewById(R.id.ratepersqtShow);
        holdingTax=findViewById(R.id.holdingtax);
        totalsqt=findViewById(R.id.ttlsqft);
        Bundle intent=getIntent().getExtras();
        if(intent!=null){
            floor=intent.getInt("floorNo",1);
            sqtpf=intent.getInt("squareFeet",100);
            tsqt=intent.getInt("totalSQT",0);
            pm=intent.getString("buildingPlacement");
            t=intent.getString("buildingType");
            c=intent.getString("buildingCategory");
            ppsqft=intent.getDouble("ratepersqt");
            ht=intent.getDouble("totalTax");
        }
        floors.setText(String.valueOf(floor));
        sqtperFloor.setText(String.valueOf(sqtpf));
        totalsqt.setText(String.valueOf(tsqt));
        placement.setText(pm);
        type.setText(t);
        category.setText(c);
        pricepersqt.setText(String.valueOf(ppsqft));
        holdingTax.setText(String.valueOf(ht));

    }
}
