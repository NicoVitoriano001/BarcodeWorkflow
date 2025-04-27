package com.app.barcodeworkflow;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.app.barcodeworkflow.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private String message = "";
    private String type = "";
    private Button button_generate;
    private EditText editText1;
    private Spinner type_spinner;
    private ImageView imageView;
    private int size = 660; //private final int size = 660;
    private int size_width = 660; //private final int size_width = 660;
    private int size_height = 264;
    private TextView success_text;
    private ImageView success_imageview;
    private String time;
    private Bitmap myBitmap;
    private Spinner codeSpinner;
    private List<String[]> rowList = new ArrayList<>();
    private List<String> spinnerList = new ArrayList<>();
    private SQLiteDatabase database;
    private String desc_item_selec; // Variável para armazenar o valor de selectedRow[1]
    private String folderLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        codeSpinner = findViewById(R.id.code_spinner); //spinner barcode
        message = "";
        type = "Barcode";

        button_generate = (Button) findViewById(R.id.generate_button);
        editText1 = (EditText) findViewById(R.id.edittext2);
        type_spinner = (Spinner) findViewById(R.id.type_spinner);
        imageView = (ImageView) findViewById(R.id.image_imageview);

        openDatabase();

        setupFilters(); // Configura os filtros e aplica a lógica inicial

        editText1.setImeOptions(EditorInfo.IME_ACTION_DONE);  // Mostra a tecla DONE

        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() { // Configura o comportamento ao pressionar a tecla ENTER ou DONE
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {  // Verifica se o usuário pressionou DONE
                    button_generate.performClick();  // Chama a ação do botão
                    return true;
                }
                return false;
            }
        });

        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: type = "QR Code"; break;
                    case 1: type = "Barcode"; break;
                    case 2: type = "Data Matrix"; break;
                    case 3: type = "PDF 417"; break;
                    case 4: type = "Barcode-39"; break;
                    case 5: type = "Barcode-93"; break;
                    case 6: type = "AZTEC"; break;
                    default: type = "Barcode"; break;
                }
                //Log.d("type", type);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não faz nada
            }
        });

        button_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editText1.getText().toString();
                if (message.equals("") || type.equals("")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Error");
                    dialog.setMessage("Invalid input!");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //  do nothing
                        }
                    });
                    dialog.create();
                    dialog.show();
                } else {  // Se não for vazio o message
                    Bitmap bitmap = null;
                    try {
                        bitmap = CreateImage(message, type);
                        myBitmap = bitmap;
                    } catch (WriterException we) {
                        we.printStackTrace();
                    }
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }

    public Bitmap CreateImage(String message, String type) throws WriterException {
        BitMatrix bitMatrix = null;
     // BitMatrix bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_128, size, size);
        switch (type)
        {
            case "QR Code": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, size, size);break;
            case "Barcode": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_128, size_width, size_height);break;
            case "Data Matrix": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.DATA_MATRIX, size, size);break;
            case "PDF 417": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.PDF_417, size_width, size_height);break;
            case "Barcode-39":bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_39, size_width, size_height);break;
            case "Barcode-93":bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_93, size_width, size_height);break;
            case "AZTEC": bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.AZTEC, size, size);break;
            default: bitMatrix = new MultiFormatWriter().encode(message, BarcodeFormat.CODE_128, size, size);break;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int [] pixels = new int [width * height];
        for (int i = 0 ; i < height ; i++)
        {
            for (int j = 0 ; j < width ; j++)
            {
                if (bitMatrix.get(j, i))
                {
                    pixels[i * width + j] = 0xff000000;
                }
                else
                {
                    pixels[i * width + j] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    public void saveBitmap (Bitmap bitmap, String message, String bitName) {
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        int permission = ContextCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS,1);
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Formato 24h - orig//int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
    //  int millisecond = calendar.get(Calendar.MILLISECOND);

        String fileName = message + "_at_" + year +  month + day + "_" + hour + minute + second;
    //  String fileName = message + "_at_" + String.valueOf(year) + "_" + String.valueOf(month) + "_" + String.valueOf(day) + "_" + String.valueOf(hour) + "_" + String.valueOf(minute) + "_" + String.valueOf(second) + "_"  + String.valueOf(millisecond);
        time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
    //  time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + millisecond;
    //  time = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second) + "." + String.valueOf(millisecond);
        File file;
        String fileLocation;
        //String folderLocation;

        if (Build.BRAND.equals("Xiaomi") ) {
            fileLocation = Environment.getExternalStorageDirectory().getPath()+"/DCIM/BarcodeWorkflow/" + fileName + bitName ;
            folderLocation = Environment.getExternalStorageDirectory().getPath()+"/DCIM/BarcodeWorkflow/"; //unifiquei em apenas um diretório
        } else {
            fileLocation = Environment.getExternalStorageDirectory().getPath()+"/DCIM/BarcodeWorkflow/" + fileName + bitName ;
            folderLocation = Environment.getExternalStorageDirectory().getPath()+"/DCIM/BarcodeWorkflow/";
        }

        //Log.d("file_location", fileLocation);

        file = new File(fileLocation);

        File folder = new File(folderLocation);
        if (!folder.exists())
        {
            folder.mkdirs();
        }

        if (file.exists())
        {
            file.delete();
        }

        FileOutputStream out;

        try
        {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out))
            {
                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveBitmap(myBitmap, message, ".jpg");
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View view = layoutInflater.inflate(R.layout.success_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("To save");
            builder.setCancelable(false);
            builder.setView(view);
            success_text = (TextView) view.findViewById(R.id.success_text);
            success_text.setText(message + "\n" + desc_item_selec + "\n\n" + time); //add desc do item. mensagem é o barcode
            success_imageview = (ImageView) view.findViewById(R.id.success_imageview);
            success_imageview.setImageBitmap(myBitmap);

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Mostrar Toast com a localização do arquivo
                    Toast.makeText(MainActivity.this,
                            "Arquivo salvo em: " + folderLocation,
                            Toast.LENGTH_LONG).show();
                    // do nothing
                }
            });
            builder.create();
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openDatabase() {
        try {
            File databasePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "barcodes.db");
            database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
        } catch (SQLiteException e) {
            Toast.makeText(this, "Erro ao abrir arquivo barcodes.db no Downloads", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private void applyFilters() {
        EditText filterColumn0 = findViewById(R.id.filter_column_0); // coluna0 loja
        EditText filterColumn2 = findViewById(R.id.filter_column_2); // coluna0 descr
        EditText filterColumn3 = findViewById(R.id.filter_column_3); // coluna3 cp

        final EditText editText1 = findViewById(R.id.edittext2);
        final Button buttonGenerate = findViewById(R.id.generate_button);

        Spinner codeSpinner = findViewById(R.id.code_spinner);

        String filter0 = filterColumn0.getText().toString().trim().toLowerCase();
        String filter2 = filterColumn2.getText().toString().trim().toLowerCase();
        String filter3 = filterColumn3.getText().toString().trim().toLowerCase();

        String[] parts = filter2.split("\\s+"); // Processa o filtro descr_imdb considerando palavras separadas
        StringBuilder filter2Processed = new StringBuilder("%");
        for (String part : parts) {
            if (!part.isEmpty()) {
                filter2Processed.append(part).append("%");
            }
        }

        String[] partss = filter3.split("\\s+"); // Processa o filtro cp considerando palavras separadas
        StringBuilder filter3Processed = new StringBuilder("%");
        for (String part : partss) {
            if (!part.isEmpty()) {
                filter3Processed.append(part).append("%");
            }
        }

        spinnerList.clear();

        final List<String[]> filteredRows = new ArrayList<>();

        if (database != null) {
            String query = "SELECT loja, barcode, descr_imdb FROM barcodes_tab WHERE LOWER(loja) LIKE ? AND LOWER(descr_imdb) LIKE ? AND LOWER(cp) LIKE ?";

            try (Cursor cursor = database.rawQuery(query, new String[]{"%" + filter0 + "%", filter2Processed.toString(), filter3Processed.toString()})) {
                while (cursor.moveToNext()) {
                    String loja = cursor.getString(0);
                    String barcode = cursor.getString(1);
                    String descrImdb = cursor.getString(2);
                    //String cp = cursor.getString(3);

                    filteredRows.add(new String[]{loja, barcode, descrImdb});
                    spinnerList.add(descrImdb); // Adiciona descr_imdb ao spinner
                }
            } catch (SQLiteException e) {
                Toast.makeText(this, "Erro ao consultar o Banco de Dados.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        codeSpinner.setAdapter(adapter);

        codeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // Listener para o Spinner
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < filteredRows.size()) {
                    String[] selectedRow = filteredRows.get(position); // Pega a linha correspondente
                    String selectedCode = selectedRow[1]; // Obtém o valor da coluna 2 do barcode
                    editText1.setText(selectedCode); // Preenche o EditText com o código selecionado
                    desc_item_selec = selectedRow[2]; //descriti para usar no bipmap
                    buttonGenerate.performClick();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada acontece caso nenhum item seja selecionado
            }

        });
    }
    private void setupFilters() {
        EditText filterColumn0 = findViewById(R.id.filter_column_0);
        EditText filterColumn2 = findViewById(R.id.filter_column_2);
        EditText filterColumn3 = findViewById(R.id.filter_column_3);

        filterColumn0.setText("6000000174x");// Define valor padrão para filterColumn0

        filterColumn0.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

      // filterColumn2.setText("Amendoim");
        filterColumn2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterColumn3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        applyFilters();  // Aplica os filtros inicialmente para preencher o spinner
    }

}