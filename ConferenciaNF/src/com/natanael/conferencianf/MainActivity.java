package com.natanael.conferencianf;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity{
 
    EditText txtUsuario;
    EditText txtSenha;
    Button entrar;
    Button change;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUsuario = (EditText) findViewById(R.id.txtUser);
        txtSenha= (EditText) findViewById(R.id.txtPassword);
        entrar= (Button) findViewById(R.id.botaoEntrar);
        
        txtSenha.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER & event.getAction() == KeyEvent.ACTION_UP)
				{
					onClickEntrar(null);
				}
				return false;
			}
		});
        
    }
    
    
    
    public void onClickEntrar(View v)
    {
        if(txtUsuario.getText().toString().compareTo("Conf")==0 && txtSenha.getText().toString().compareTo("Conf")==0)
        {
            txtUsuario.setText("");
            txtSenha.setText("");
            Intent intent = new Intent(this,ConferenciaNF.class);
            MainActivity.this.startActivity(intent);
            this.finish();
            
            
        }
        else
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
            	.setTitle("Erro ao entrar")
        		.setMessage("Usuário ou senha errados")
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	txtUsuario.setText("");
                    	txtSenha.setText("");
                    	txtUsuario.requestFocus();
                    }
                }).show();
        }
    }


    
}
