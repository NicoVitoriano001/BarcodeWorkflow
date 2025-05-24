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
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String message = "", type = "", time, folderLocation, desc_item_selec;
    private static final String BACKUP_FOLDER = "BC_WORKFLOW";
    private Button button_generate;
    private EditText editText1;
    private Spinner type_spinner;
    private int size = 660, size_width = 660, size_height = 264; //private final int size = 660;
    private TextView success_text;
    private ImageView success_imageview, imageView;
    private Bitmap myBitmap;
    private Spinner codeSpinner;
    private List<String[]> rowList = new ArrayList<>();
    private List<String> spinnerList = new ArrayList<>();
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        codeSpinner = findViewById(R.id.spinnerItem); //spinner barcode
        message = "";
        type = "Barcode";

        button_generate = (Button) findViewById(R.id.generate_button);
        editText1 = (EditText) findViewById(R.id.editTextNumberBarcode);
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
        int month = calendar.get(Calendar.MONTH) + 1; // MONTH começa em 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Formato 24h - orig//int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
    //  int millisecond = calendar.get(Calendar.MILLISECOND);

        String fileName = message + "_at_" + year +  month + day + "_" + hour + minute + second;
        time = String.format(Locale.getDefault(),"%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second); // dois digitos
        //time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
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
        if (item.getItemId() == R.id.action_save) {
            // Verifica se o bitmap e a mensagem são válidos
            if (myBitmap == null || myBitmap.isRecycled()) {
                Toast.makeText(this, "Bitmap inválido ou não gerado", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Nenhum código gerado para salvar", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Prepara o texto com todas as informações
            String infoText = String.format(Locale.getDefault(),
                    "%s\n%s\n\n%s",
                    message,
                    desc_item_selec != null ? desc_item_selec : "",
                    time != null ? time : "");

            // Infla o layout de sucesso
            View dialogView = LayoutInflater.from(this).inflate(R.layout.success_dialog, null);
            TextView successText = (TextView) dialogView.findViewById(R.id.success_text);
            ImageView successImageView = (ImageView) dialogView.findViewById(R.id.success_imageview);

            successText.setText(infoText);
            successImageView.setImageBitmap(myBitmap);

            // Cria e mostra o diálogo de confirmação
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar salvamento")
                    //.setMessage("Deseja salvar este código?")
                    .setView(dialogView)
                    .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveBitmap(myBitmap, message, ".jpg");
                            Toast.makeText(MainActivity.this,
                                    "Arquivo salvo em: " + folderLocation,
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDatabase() {
        try {
            // Caminho para o diretório de Downloads + sua pasta personalizada
            File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), BACKUP_FOLDER);

            // Verifica se o diretório existe, se não, cria
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    Toast.makeText(this, "Não foi possível criar o diretório", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            // Caminho completo para o arquivo de banco de dados
            File databaseFile = new File(backupDir, "barcodes.db");
           // COLUNAS NO BANCO DE DADOS loja TEXT, barcode NUMERIC, descr_imdb TEXT,cp TEXT

            // Verifica se o arquivo existe
            if (!databaseFile.exists()) {
                Toast.makeText(this, "Arquivo barcodes.db não encontrado em " + backupDir.getPath(), Toast.LENGTH_LONG).show();
                return;
            }
            // Abre o banco de dados
            database = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

        } catch (SQLiteException e) {
            Toast.makeText(this, "Erro ao abrir o banco de dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        }
    }
    private void applyFilters() {
        EditText filterColumn0 = findViewById(R.id.filter_column_0); // coluna0 loja
        EditText filterColumn2 = findViewById(R.id.filter_column_2); // coluna2 descr
        EditText filterColumn3 = findViewById(R.id.filter_column_3); // coluna3 cp

        final EditText editText1 = findViewById(R.id.editTextNumberBarcode);
        final Button buttonGenerate = findViewById(R.id.generate_button);

        Spinner codeSpinner = findViewById(R.id.spinnerItem);

        String filter0 = filterColumn0.getText().toString().trim().toLowerCase(); // coluna0 loja
        String filter2 = filterColumn2.getText().toString().trim().toLowerCase(); // coluna2 descr
        String filter3 = filterColumn3.getText().toString().trim().toLowerCase(); // coluna3 cp

        String[] descrCol2 = filter2.split("\\s+"); // Processa o filtro descr_imdb considerando palavras separadas
        StringBuilder descrCol2_comLike = new StringBuilder("%");
        for (String part : descrCol2) {
            if (!part.isEmpty()) {
                descrCol2_comLike.append(part).append("%");
            }
        }

        String[] cpCol3 = filter3.split("\\s+"); // Processa o filtro cp considerando palavras separadas
        StringBuilder cpCol3_comLike = new StringBuilder("%");
        for (String part : cpCol3) {
            if (!part.isEmpty()) {
                cpCol3_comLike.append(part).append("%");
            }
        }

        spinnerList.clear();

        final List<String[]> filteredRows = new ArrayList<>();

        if (database != null) {
            String query = "SELECT loja, barcode, descr_imdb FROM barcodes_tab WHERE LOWER(loja) LIKE ? AND LOWER(descr_imdb) LIKE ? AND LOWER(cp) LIKE ?";

            try (Cursor cursor = database.rawQuery(query, new String[]{"%" + filter0 + "%", descrCol2_comLike.toString(), cpCol3_comLike.toString()})) {
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
                //e.printStackTrace();
            }
        }

        MultilineSpinnerAdapter adapter = new MultilineSpinnerAdapter(this,
                android.R.layout.simple_spinner_item, spinnerList);
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


    // Adicione como variável de classe
    private Handler handler = new Handler();
    private Runnable filterRunnable = new Runnable() {
        @Override
        public void run() {
            applyFilters();
        }
    };
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(filterRunnable);
        super.onDestroy();
    }

    private void setupFilters() {
        EditText filterColumn0 = findViewById(R.id.filter_column_0); //LOJA //1BARCODE
        EditText filterColumn2 = findViewById(R.id.filter_column_2); //DESC
        EditText filterColumn3 = findViewById(R.id.filter_column_3); //CP

        filterColumn0.setText("60W0045455");// Define valor padrão para filterColumn0
        filterColumn0.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(filterRunnable);
                if (s.length() >= 4) {  // Só filtra com pelo menos 3 caracteres
                    handler.postDelayed(filterRunnable, 300); //Filtragem somente após 300ms de inatividade
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        filterColumn2.setText("CLARA 500");// Define valor padrão para filterColumn0
        filterColumn2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(filterRunnable);
                if (s.length() >= 4) {  // Só filtra com pelo menos 3 caracteres
                    handler.postDelayed(filterRunnable, 300);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
        filterColumn3.setText("CAFE");// Define valor padrão para filterColumn0
        filterColumn3.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(filterRunnable);
                if (s.length() >= 4) {  // Só filtra com pelo menos 3 caracteres
                    handler.postDelayed(filterRunnable, 300);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        applyFilters();  // Aplica os filtros inicialmente para preencher o spinner
    }

}