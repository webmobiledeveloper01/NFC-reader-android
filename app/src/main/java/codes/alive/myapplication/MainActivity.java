package codes.alive.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.google.gson.Gson;

import net.sf.scuba.util.Hex;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CARD-Detail";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] techListArray;
    private IntentFilter[]intentFiltersArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCard=findViewById(R.id.txtCard);

        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this,"NFC Hardware not available on Device",Toast.LENGTH_SHORT).show();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,"NFC is NOT Enabled, Please Enable NFC",Toast.LENGTH_SHORT).show();
        }

        Intent intent=new Intent(this,getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_MUTABLE);

        intentFiltersArray=new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};

        String[] ss=new String[1];
        techListArray=new String[1][];
        ss[0]= NfcA.class.getName();
        techListArray[0]=ss;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter!=null){
            nfcAdapter.enableForegroundDispatch(MainActivity.this,pendingIntent,intentFiltersArray,techListArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private TextView txtCard;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tagFromIntent=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        IsoDep tag = IsoDep.get(tagFromIntent);

        try {

        tag.connect();

        IProvider provider=new Provider(tag);

        EmvTemplate.Config config = EmvTemplate.Config()
                .setContactLess(true) // Enable contact less reading (default: true)
                .setReadAllAids(true) // Read all aids in card (default: true)
                .setReadTransactions(true) // Read all transactions (default: true)
                .setReadCplc(false) // Read and extract CPCLC data (default: false)
                .setRemoveDefaultParsers(false) // Remove default parsers for GeldKarte and EmvCard (default: false)
                .setReadAt(true) // Read and extract ATR/ATS and description
                ;
// Create Parser
        EmvTemplate parser = EmvTemplate.Builder() //
                .setProvider(provider) // Define provider
                .setConfig(config) // Define config
//                .setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
                .build();

// Read card
            EmvCard card = parser.readEmvCard();


            String ss="";
            ss+=card.getCardNumber()+"\n";
            ss+=card.getApplications().get(0).getApplicationLabel()+"\n";
            ss+=card.getType().getName()+"\n";

            SimpleDateFormat sdf=new SimpleDateFormat("MM-yyyy");
            ss+=sdf.format(card.getExpireDate());

            txtCard.setText(txtCard.getText()+"\n\n"+ss);

//            SystemClock.sleep(1000);

//            tag.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                tag.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}