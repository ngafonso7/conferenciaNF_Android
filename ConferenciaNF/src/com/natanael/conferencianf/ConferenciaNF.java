package com.natanael.conferencianf;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ConferenciaNF extends Activity {

	EditText txtCnpj;
    EditText txtForn;
    EditText txtNumNF;
    EditText txtChaveAcesso;
    EditText txtDataEmissao;
    EditText txtserieNF;
    
    Spinner listaModelo;
    
    
    ListView listaProd;
    
    Button botaoInserir;
    Button botaoPesquisar;
    Button botaoGravar;
    Button botaoEditar;
    Button botaoRemover;
    Button botaoLimpar;

    //static String ipServer = "192.168.0.174";
    static String ipServer = "192.168.0.125";
    static String portaServer = "12345";
    
    String codFornecedor = "";

    static Socket server;

    public Boolean testServer = true;
    
    String codProd = "";
    
    Boolean inserir = false;
    Boolean insert = false;
    Boolean gravacao = false;
    Boolean consultaChave = false;
    
    Boolean spinnerLoad = true;
    Boolean spinLoad = true;
    
    Boolean ContinuaInserir = true;

    int selectedItem = -1;
    
    
    Handler handler;
    
    InputStream entrada = null;
    OutputStream saida = null;

    static ProgressDialog progressBar;
    private boolean progressBarStatus = false;
    private Handler progressBarHandler = new Handler();
    
    ArrayList<String> itens;
    ArrayList<String> codProduto;
    ArrayList<String> descProduto;
    ArrayList<String> quantProtudo;
    
    ArrayList<String> modelo;
    ArrayList<String> serie;
    
    ArrayAdapter<String> adapter;
    
    public ConferenciaNF ()
    {
    	
    	
	            
    	
    	handler = new Handler() {
			  @Override
			  public void handleMessage(Message msg) {
				 
				  	Bundle bundle = new Bundle();
				  	bundle = msg.getData();
				  	if(bundle.get("cmd").toString().compareTo("MSG")==0)
				  	{
					  	showErro(bundle.get("msg").toString(),bundle.get("title").toString() ,false);
					  	
					  	if(bundle.getBoolean("FIM"))
					  	{
					  		aguarde_end();
					  	}
				  	}
				  	else if(bundle.get("cmd").toString().compareTo("MSG-F")==0)
				  	{
					  	showErro(bundle.get("msg").toString(),bundle.get("title").toString() ,true);
				  	}
				  	else if(bundle.get("cmd").toString().compareTo("CLT")==0)//Comando limpa tela
				  	{
				  		
				  		inserir = false;
				  		
				  		codProduto.clear();
						descProduto.clear();
						quantProtudo.clear();
						
						itens.clear();
	                    adapter = null;
	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	                    listaProd.setAdapter(adapter);
	                    
	                    txtCnpj.setText("");
	                    txtForn.setText("");
	                    txtNumNF.setText("");
	                    txtserieNF.setText("");
	                    txtDataEmissao.setText("");
	                    txtChaveAcesso.setText("");
	                    
	                    listaModelo.setSelection(0);
	                    
	                    botaoGravar.setEnabled(false);
	                    botaoInserir.setEnabled(false);
	                    botaoPesquisar.setEnabled(false);
	                    botaoEditar.setEnabled(false);
	                    botaoRemover.setEnabled(false);
	                    botaoLimpar.setEnabled(false);
	                    //txtCnpj.setFocusable(false);
	                    txtForn.setFocusable(false);
	                    txtForn.setFocusableInTouchMode(false);
	                    txtNumNF.setFocusable(false);
	                    txtNumNF.setFocusableInTouchMode(false);
	                    txtserieNF.setFocusable(false);
	                    txtserieNF.setFocusableInTouchMode(false);
	                    listaModelo.setFocusable(false);
	                    listaModelo.setFocusableInTouchMode(false);
	                    //txtChaveAcesso.setFocusable(false);
	                    txtDataEmissao.setFocusable(false);
	                    txtDataEmissao.setFocusableInTouchMode(false);
	                    
	                    txtCnpj.setFocusable(true);
	                    txtCnpj.setFocusableInTouchMode(true);
	                    
	                    txtChaveAcesso.setFocusable(true);
	                    txtChaveAcesso.setFocusableInTouchMode(true);
	                    txtChaveAcesso.requestFocus();
				  	}
			     }
			 };

    	conectar(ipServer,portaServer);
    	itens = new ArrayList<String>();
    	codProduto = new ArrayList<String>();
        descProduto = new ArrayList<String>();
        quantProtudo = new ArrayList<String>();
        modelo = new ArrayList<String>();
        
        
        
        aguarde_end();
        
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conferencia);

        progressBar = new ProgressDialog(ConferenciaNF.this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Aguarde ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(20);

        txtCnpj = (EditText) findViewById(R.id.txtCNPJ);
        txtForn = (EditText) findViewById(R.id.txtFornecedor);
        txtNumNF = (EditText) findViewById(R.id.txtNumNF);
        txtserieNF = (EditText) findViewById(R.id.txtSerieNF);
        txtChaveAcesso = (EditText) findViewById(R.id.txtChaveAcesso);
        txtDataEmissao = (EditText) findViewById(R.id.txtDataEmissao);
        listaModelo = (Spinner) findViewById(R.id.txtModeloNF);
        listaProd = (ListView) findViewById(R.id.listaProd);
        
        botaoInserir = (Button) findViewById(R.id.botaoInserir);
        botaoPesquisar = (Button) findViewById(R.id.botaoPesquisar);
        botaoGravar = (Button) findViewById(R.id.botaoGravar);
        botaoEditar = (Button) findViewById(R.id.botaoEditar);
        botaoRemover = (Button) findViewById(R.id.botaoRemover);
        botaoLimpar = (Button) findViewById(R.id.botaoLimpar);
        
        botaoInserir.setEnabled(inserir);
        botaoPesquisar.setEnabled(inserir);
        botaoGravar.setEnabled(inserir);
        botaoEditar.setEnabled(inserir);
        botaoRemover.setEnabled(inserir);
        botaoLimpar.setEnabled(inserir);
        
        
        txtCnpj.setText("");
        txtChaveAcesso.setText("");
        txtChaveAcesso.setFocusable(true);
        txtChaveAcesso.requestFocus();
        
        modelo.add("Selecione um Modelo ");
        modelo.add("55");
        modelo.add("01");
        modelo.add("04");
        
        adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, modelo);
        listaModelo.setAdapter(adapter);
        
        
        spinnerLoad = true;
        
        //txtCnpj.setFocusable(false);
        txtForn.setFocusable(false);
        txtNumNF.setFocusable(false);
        txtserieNF.setFocusable(false);
        //txtChaveAcesso.setFocusable(false);
        txtDataEmissao.setFocusable(false);
        
        txtCnpj.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_UP & txtCnpj.getText().toString().compareTo("")!= 0)
				{
					String cnpj = txtCnpj.getText().toString();
					
					if(cnpj.length() == 18)
					{
						if(cnpj.charAt(10) == '/')
						{
							String c1 = cnpj.substring(0,2);
			                String c2 = cnpj.substring(3,6);
			                String c3 = cnpj.substring(7,10);
			                String c4 = cnpj.substring(11,15);
			                String c5 = cnpj.substring(16,18);
			                txtCnpj.setText(c1+c2+c3+c4+c5);
			                txtCnpj.selectAll();
						}
					}
					else if (cnpj.length() == 14)
					{
						if(cnpj.charAt(11)== '-')
						{
							String c1 = cnpj.substring(0,3);
			                String c2 = cnpj.substring(4,7);
			                String c3 = cnpj.substring(8,11);
			                String c4 = cnpj.substring(12,14);
			                txtCnpj.setText(c1+c2+c3+c4);
			                txtCnpj.selectAll();
						}
					}
				}
				return false;
			}
		});
        
        txtCnpj.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
                {
                    if(txtCnpj.getText().toString().length() == 14)
                    {
                        String cnpj = txtCnpj.getText().toString();
                        String c1 = cnpj.substring(0,2);
                        String c2 = cnpj.substring(2,5);
                        String c3 = cnpj.substring(5,8);
                        String c4 = cnpj.substring(8,12);
                        String c5 = cnpj.substring(12,14);

                        txtCnpj.setText(c1 + "." + c2 + "." + c3 + "/" + c4 + "-" + c5);
                        
                        //((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        //.hideSoftInputFromWindow(txtCnpj.getWindowToken(), 0);
                        consultaFornecedor(txtCnpj.getText().toString());
                        
                        	

                    }
                    else if(txtCnpj.getText().toString().length() == 11)
                    {
                        String cpf = txtCnpj.getText().toString();
                        String c1 = cpf.substring(0,3);
                        String c2 = cpf.substring(3,6);
                        String c3 = cpf.substring(6,9);
                        String c4 = cpf.substring(9,11);

                        txtCnpj.setText(c1 + "." + c2 + "." + c3 + "-" + c4);

                        //((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        //.hideSoftInputFromWindow(txtCnpj.getWindowToken(), 0);
                        consultaFornecedor(txtCnpj.getText().toString());
                        
                        
                    }
                    else
                    {
                    	showErro("CPNJ/CPF inválido","Consulta Fornecedor",false);
                    	txtCnpj.selectAll();
                    	
                    	return false;
                    }
                    
                }
                return false;
            }
        });
        
        txtChaveAcesso.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent keyEvent) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP)
				{
					if(txtChaveAcesso.getText().length() == 44)
					{
						if(!consultaChave)
						{
							decodificaChaveAcesso(txtChaveAcesso.getText().toString());
						}else
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
        	                builder
        	                	.setTitle("Consultar Chave de Acesso")
        	            		.setMessage("Deseja mesmo consultar outra chave de acesso ?")
        	                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        }
        	                    })
        	                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        	consultaChave = false;
        	                        	decodificaChaveAcesso(txtChaveAcesso.getText().toString());
        	                        	
        	                        }
        	                    })
        	                    .show();
						}
						
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
    	                builder
    	                	.setTitle("Chave de Acesso")
    	            		.setMessage("A chave de acesso deve ter 44 digitos !")
    	                    
    	                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	                        public void onClick(DialogInterface dialog, int which) {
    	                        	txtChaveAcesso.selectAll();
    	                        	
    	                        }
    	                    })
    	                    .show();
					}
				}
				else 
				{
					consultaChave = false;
				}
				return false;
			}
		});
        
        
        
        botaoInserir.setOnClickListener(new View.OnClickListener() {
			
        	
        	
			@Override
			public void onClick(View arg0) {
				
				inserirCodigo();

			}
		});
        
        botaoPesquisar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
								
				AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);

				insert = false;
				// Set up the input
				final EditText input = new EditText(ConferenciaNF.this);
				
				builder.setTitle("Descrição do Produto");
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	if(input.getText().toString().compareTo("")!=0)
				    		pesquisarProdutoDescricao(input.getText().toString());
				    	dialog.cancel();
				    	
				    }

					
				});
				final AlertDialog alert = builder.create();
				alert.show();
				
				input.setOnKeyListener(new View.OnKeyListener() {
					
					@Override
					public boolean onKey(View arg0, int keyCode, KeyEvent keyEvent) {
						if (keyCode == 66 && insert == false && keyEvent.getAction() == KeyEvent.ACTION_UP)
							{
							insert = true;
							inserirProduto(input.getText().toString());
							alert.dismiss();
							
						}
						return false;
					}
				});
				
			}
		});
        
       
        
        botaoGravar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
                builder
                	.setTitle("Gravação NF")
            		.setMessage("Deseja mesmo gravar esta nota ?")
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	String nNF = txtNumNF.getText().toString();
            				String modNF = listaModelo.getItemAtPosition(listaModelo.getSelectedItemPosition()).toString();
            				String serNF = txtserieNF.getText().toString();
            				String datNF = txtDataEmissao.getText().toString();
            				String chNF = txtChaveAcesso.getText().toString();
            				if(itens.size() > 0) 
            				{
            					if(nNF.compareTo("")!= 0 & modNF.compareTo("")!= 0 & datNF.compareTo("")!= 0)
            					{
            						if(modNF.compareTo("55")==0 )
            						{
            							if((serNF.compareTo("")==0))
            								showErro("Série Nota Fiscal obrigatória para nota fiscal modelo 55 !", "Gravação NF", false);
            							else if(chNF.compareTo("")== 0)
            								showErro("Chave de Acesso obrigatória para nota fiscal modelo 55 !", "Gravação NF", false);
            							else
            								if(validaData())
            									gravaItensNF();
            								else
            									showErro("Data de Emissão inválida", "Gravação NF", false);
	            					}
            						else
            							showErro("Modelo da NF Inválido !", "Gravação NF", false);
            						
            					}
            					else
            						showErro("Existem campos em branco !", "Gravação NF", false);
            				}
            				else
            					showErro("Não existem produtos para gravação !", "Gravação NF", false);
                        }
                    })
                    .show();
                
			}
		});
        
        botaoRemover.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				final int pos = selectedItem;
				if(pos != -1)
				{
					String dados[] = listaProd.getItemAtPosition(pos).toString().split("-");
					
					String nomeProduto = dados[1];
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
	                builder
	                	.setTitle("Remover Produto")
	            		.setMessage("Deseja mesmo remover o produto: " +nomeProduto + " ?")
	                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        }
	                    })
	                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        	itens.remove(pos);
	                        	codProduto.remove(pos);
	    						descProduto.remove(pos);
	    						quantProtudo.remove(pos);
	    	                    adapter = null;
	    	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	    	                    listaProd.setAdapter(adapter);
	                        }
	                    })
	                    .show();
				}
				else
					showErro("Selecione um item da lista", "Remoção de Item", false);
				
			}
		});
        
        botaoEditar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final int pos = selectedItem;
				if(pos != -1)
				{
					String dados[] = listaProd.getItemAtPosition(pos).toString().split("-");
					
					final String nomeProduto = dados[1];
					final String codigoProduto = dados[0];
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
	                builder
	                	.setTitle("Editar Produto")
	            		.setMessage("Deseja mesmo editar o produto: " +nomeProduto + " ?")
	                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        }
	                    })
	                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        	editarQuantidadeItem(codigoProduto, nomeProduto, pos);
	                        	
	                        }
	                    })
	                    .show();
				}
				else
					showErro("Selecione um item da lista", "Edição de Item", false);
				
				
				
			}
		});
        
        botaoLimpar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
                builder
                	.setTitle("Limpar Tela")
            		.setMessage("Deseja mesmo limpar a tela?")
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	Message msg = handler.obtainMessage();
                        	Bundle bundle = new Bundle();
                        	bundle.putString("cmd", "CLT");//Comando limpa tela
                        	msg.setData(bundle);
                        	handler.sendMessage(msg);
                        }
                    })
                    .show();
				
				
				
			}
		});
        
        txtDataEmissao.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				if(txtDataEmissao.getText().length()==2 & keyCode != KeyEvent.KEYCODE_DEL)
				{
					//txtDataEmissao.setText(txtDataEmissao.getText().subSequence(0, 2)+"/");
					txtDataEmissao.append("/");
				}
				else if(txtDataEmissao.getText().length()==5 & keyCode != KeyEvent.KEYCODE_DEL)
				{
					//txtDataEmissao.setText(txtDataEmissao.getText().subSequence(0, 5)+"/");
					txtDataEmissao.append("/");
				}
				else
				{
					if(txtDataEmissao.getText().toString().compareTo("") != 0)
					{
						if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
						{
							
							if(!validaData())
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
				                builder
				                	.setTitle("Data de Emissão")
				            		.setMessage("Data inválida !")
				                    
				                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				                        public void onClick(DialogInterface dialog, int which) {
				                        	
				                        	txtDataEmissao.setFocusable(true);
				                        	txtDataEmissao.setFocusableInTouchMode(true);
				                        	txtDataEmissao.requestFocus();
				                        	txtDataEmissao.selectAll();
				                        	
				                        }
				                    })
				                    .show();
							}
							if(txtChaveAcesso.getText().toString().compareTo("")==0)
							{
								txtChaveAcesso.setFocusable(true);
								txtChaveAcesso.setFocusableInTouchMode(true);
								txtChaveAcesso.requestFocus();
							}
						}
					}
				}
				return false;
			}
		});
        
        txtNumNF.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER & event.getAction()== KeyEvent.ACTION_UP)
				{
					listaModelo.setFocusableInTouchMode(true);
					listaModelo.setFocusable(true);
					listaModelo.requestFocus();
					listaModelo.performClick();
					txtNumNF.clearFocus();
					return true;
				}
				return false;
			}
		});
        
        
        listaModelo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	if(spinnerLoad)
            		spinnerLoad = false;
            	else
            	{
            		if(txtChaveAcesso.getText().toString().compareTo("")==0 & txtCnpj.getText().toString().compareTo("")!= 0)
            		{
	            		txtserieNF.setFocusableInTouchMode(true);
	            		txtserieNF.setFocusable(true);
		                txtserieNF.requestFocus();
		            }
	                
            	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        txtserieNF.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER & event.getAction()== KeyEvent.ACTION_UP)
				{
					txtDataEmissao.setFocusableInTouchMode(true);
					txtDataEmissao.setFocusable(true);
					txtDataEmissao.requestFocus();
					
					return true;
				}
				return false;
			}
		});
        
        listaProd.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				int pos = arg2;
				if(pos != -1)
				{
					selectedItem = pos;
				}
				else
				{
					selectedItem = -1;
				}
				
			}
		});    
        
        listaProd.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int index, long arg3) {
				
				String dados[] = listaProd.getItemAtPosition(index).toString().split("-");
				
				final String nomeProduto = dados[1];
				final String codigoProduto = dados[0];
				final int pos = index;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
                builder
                	.setTitle("Opções do Produto")
            		.setMessage("Deseja mesmo editar o produto: " +nomeProduto + " ?")
                    .setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	
                        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
        	                builder
        	                	.setTitle("Editar Produto")
        	            		.setMessage("Deseja mesmo editar o produto: " +nomeProduto + " ?")
        	                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        }
        	                    })
        	                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        	editarQuantidadeItem(codigoProduto, nomeProduto, pos);
        	                        	
        	                        }
        	                    })
        	                    .show();
                        }
                    })
                    .setNeutralButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
        	                builder
        	                	.setTitle("Remover Produto")
        	            		.setMessage("Deseja mesmo remover o produto: " +nomeProduto + " ?")
        	                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        }
        	                    })
        	                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
        	                        public void onClick(DialogInterface dialog, int which) {
        	                        	itens.remove(pos);
        	                        	codProduto.remove(pos);
        	    						descProduto.remove(pos);
        	    						quantProtudo.remove(pos);
        	    	                    adapter = null;
        	    	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
        	    	                    listaProd.setAdapter(adapter);
        	                        }
        	                    })
        	                    .show();
                        	
                        }
                    })
                    .setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//
							
						}
					})
                    .show();
				return false;
			}
		});
        
    }
    
    public Boolean validaData()
    {
    	try{
			if(txtDataEmissao.getText().toString().length() == 10)
			{
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
				dateFormat.setLenient(false);
				String data = dateFormat.format(dateFormat.parse(txtDataEmissao.getText().toString())).toString();
						
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(txtDataEmissao.getWindowToken(), 0);
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
    }
    
    public void decodificaChaveAcesso(String chave)
    {
    	
		String cnpjCh = chave.substring(6,20);
		String numnfCh = chave.substring(25,34);
		String modCh = chave.substring(20,22);
		String serieCh = chave.substring(22,25);
		
		String c1 = cnpjCh.substring(0,2);
        String c2 = cnpjCh.substring(2,5);
        String c3 = cnpjCh.substring(5,8);
        String c4 = cnpjCh.substring(8,12);
        String c5 = cnpjCh.substring(12,14);

        txtCnpj.setText(c1 + "." + c2 + "." + c3 + "/" + c4 + "-" + c5);
        consultaFornecedor(txtCnpj.getText().toString());
		
		txtNumNF.setText(String.valueOf(Integer.parseInt(numnfCh)));
		txtserieNF.setText(String.valueOf(Integer.parseInt(serieCh)));
		if(modCh.compareTo("55")==0)
		{
			listaModelo.setSelection(1);
		}
		else if(modCh.compareTo("04")==0)
		{
			listaModelo.setSelection(2);
		}
		else
		{
			listaModelo.setSelection(3);
		}
		txtCnpj.setFocusableInTouchMode(false);
		txtCnpj.setFocusable(false);
		txtChaveAcesso.setFocusableInTouchMode(false);
		txtChaveAcesso.setFocusable(false);
		txtDataEmissao.setFocusableInTouchMode(true);
		txtDataEmissao.setFocusable(true);
		txtDataEmissao.requestFocus();
		consultaChave = true;
    }
	    private void pesquisarProdutoDescricao(String descr){
	    	ArrayList<String> listaPesquisa = new ArrayList<String>();
	    	
	    	
	    	try{
		    	aguarde_init();
	            if(descr.compareTo("") != 0)
	            {
	                conectar(ipServer,portaServer);
	                enviar("CPP\n"+descr);
	                String palavra = "";
	                String dados[];
	                listaPesquisa.clear();
	                while((palavra = recebeCaracteres()).compareTo("CPPI")==0)
	                {
	                	dados = recebeCaracteres().split(";");
	                	listaPesquisa.add(dados[0] + " - " + dados[1]);
	                }
	                aguarde_end();
	                if(palavra.compareTo("CPPF")==0 & listaPesquisa.size() >0)
	                {
				    	
				    	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);

						insert = false;
						// Set up the input
						final Spinner listagem = new Spinner(ConferenciaNF.this);
						
						builder.setTitle("Listagem do Produto");
						
						adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, listaPesquisa);
						
						listagem.setAdapter(adapter);
						
						builder.setView(listagem);
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						        dialog.cancel();
						    }
						});
						builder.setPositiveButton("Selecionar", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								String dados[] = listagem.getItemAtPosition(listagem.getSelectedItemPosition()).toString().split("-");
								dialog.cancel();
								inserirProduto(dados[0]);
								
							}
						});
			
						final AlertDialog alert = builder.create();
						alert.show();
						
	                }
	                else
	                {
	                	showErro("Nenhum produto encontrado !", "Pesquisa Produto",false);
	                }
	            }
	    	}catch(Exception e ){
	    		aguarde_end();
	    	}

			
		}
    
	    
	    public void fechaDialogo(AlertDialog alert)
	    {
	    	alert.dismiss();
	    }
	    
	    public void gravaItensNF()
	    {
	    	aguarde_init();
	    	new Thread(new Runnable() {
				
				@Override
				public void run() {
					conectar(ipServer,portaServer);
					
					enviar("CABNF");
					
					String nNF = txtNumNF.getText().toString();
    				String modNF = listaModelo.getItemAtPosition(listaModelo.getSelectedItemPosition()).toString();
    				String serNF = txtserieNF.getText().toString();
    				String datNF = txtDataEmissao.getText().toString();
    				String chNF = txtChaveAcesso.getText().toString();
    				
    				enviar(codFornecedor + ";" + nNF + ";" + modNF + ";" + serNF + ";" + datNF + ";" + chNF);
					gravacao = false;
    				receber_t();
    				if(gravacao)
    				{
				    	enviar("GIN");
				    	for(int i=0;i< itens.size();i++)
				    	{
				    		gravacao = false;
				    		enviar(codProduto.get(i) + ";" + quantProtudo.get(i) );
				    		receber_t();
				    		/*if(!gravacao)
				    		{
				    			i = itens.size();
				    			Message msg = handler.obtainMessage();
	                        	Bundle bundle = new Bundle();
	                        	bundle.putString("cmd", "MSG");
	                        	bundle.putString("msg", "Erro na gravação de dados");
	                        	bundle.putString("title", "Gravação NF");
	                        	bundle.putBoolean("FIM", true);
	                        	msg.setData(bundle);
	                        	handler.sendMessage(msg);
				    			//showErro("Erro na gravação dos dados", "Gravação NF", false);
				    		}*/
				    	}
				    	if(gravacao)
				    	{
				    		enviar("FIM");
				    		receber_t();
				    	}
    				}
    				else
    				{
    					Message msg = handler.obtainMessage();
                    	Bundle bundle = new Bundle();
                    	bundle.putString("cmd", "MSG");
                    	bundle.putString("msg", "Erro na gravação do cabeçalho da NF");
                    	bundle.putString("title", "Gravação NF");
                    	bundle.putBoolean("FIM", true);
                    	msg.setData(bundle);
                    	handler.sendMessage(msg);
    				}
				}
			}).start();
	    	
	    	
	    }
	    
	    
    	public void inserirProduto(String codigo)
    	{
    		aguarde_init();
    		if(codigo.compareTo("")!= 0)
    		{
    			conectar(ipServer,portaServer);
    			enviar("CIP\n" + codigo);
                receber();
    		}
            else
            {
                showErro("Código produto vazio","Consulta Produto",false);
    		}
    		aguarde_end();
            
    	}
    
        public void consultaFornecedor(String cnpj)
        {
        	aguarde_init();
            if(cnpj.compareTo("") != 0)
            {
                conectar(ipServer,portaServer);
                enviar("CNF\n"+cnpj);
                receber();
                
            }
            aguarde_end();
        }

        public void conectar(String ip, String porta){
            try{

                server = new Socket(ip,Integer.parseInt(porta));

                entrada = server.getInputStream();
                saida = server.getOutputStream();
                if(testServer)
                {
                    enviar("Test Server");
                    Thread.sleep(500);
                    testServer = false;
                }

                String palavra = "";

                Boolean conexaoOk = false;

                while(!conexaoOk)
                {
                	palavra = recebeCaracteres();
                    conexaoOk = true;
                    if(palavra.compareTo("Conexao OK")!=0)
                    {
                    	//showErro("Servidor não respondeu !","Sem conexão",true);
                        //System.exit(0);
                    	Message msg = handler.obtainMessage();
                    	Bundle bundle = new Bundle();
                    	bundle.putString("cmd", "MSG-F");
                    	bundle.putString("msg", "Erro na conexão com Servidor");
                    	bundle.putString("title", "Conexão Servidor");
                    	bundle.putBoolean("FIM", true);
                    	msg.setData(bundle);
                    	handler.sendMessage(msg);
                    }
                }

            }catch(Exception e){
                //showErro("Servidor não respondeu !","Sem conexão",true);
            	Message msg = handler.obtainMessage();
            	Bundle bundle = new Bundle();
            	bundle.putString("cmd", "MSG-F");
            	bundle.putString("msg", "Erro na conexão com Servidor");
            	bundle.putString("title", "Conexão Servidor");
            	bundle.putBoolean("FIM", true);
            	msg.setData(bundle);
            	handler.sendMessage(msg);

            }

        }

        public void enviar(String msg){
            try{
                if(server != null)
                {
                    byte[] b = (msg+"\n").getBytes();

                    if(saida != null)
                    {

                        saida.write(b);
                        saida.flush();
                    }
                }
            }catch(Exception e){
                Toast.makeText(ConferenciaNF.this, e.toString(), Toast.LENGTH_LONG ).show();
            }
        }

        public void receber_t(){
            try{
                //aguarde_init();
                if(this.server != null)
                {
                    entrada = server.getInputStream();
                    saida = server.getOutputStream();
                    if(this.entrada != null)
                    {
                        String palavra="";
                        palavra = recebeCaracteres();
                        palavra.replace("\n","");
                        
                        if(palavra.compareTo("Gravacao OK")==0)
                        {
                        	gravacao = true;
                        }
                        else if(palavra.compareTo("CABNFOK")==0)
                        {
                        	gravacao = true;
                        }
                        else if(palavra.compareTo("GINF")==0)
                        {
                        	Message msg = handler.obtainMessage();
                        	Bundle bundle = new Bundle();
                        	bundle.putString("cmd", "MSG");
                        	bundle.putString("msg", "Gravação de dados da NF Concluida");
                        	bundle.putString("title", "Gravação NF");
                        	bundle.putBoolean("FIM", true);
                        	msg.setData(bundle);
                        	handler.sendMessage(msg);
                        	
                        	msg = handler.obtainMessage();
    	                	bundle = new Bundle();
    	                	bundle.putString("cmd", "CLT");//Comando limpa tela
    	                	msg.setData(bundle);
    	                	handler.sendMessage(msg);
    	                	
    	                	inserir = false;
    	                	consultaChave = false;
    	                	
    	                	
                        	
                        	//showErro("Gravação de dados da NF concluida.","Gravação NF",false);
                        	
                        }
                        else if(palavra.compareTo("Falha GIND")==0)
                        {
                        	gravacao = false;
                        	Message msg = handler.obtainMessage();
                        	Bundle bundle = new Bundle();
                        	bundle.putString("cmd", "MSG");
                        	bundle.putString("msg", "Erro na gravação de dados, dados insuficientes");
                        	bundle.putString("title", "Gravação NF");
                        	bundle.putBoolean("FIM", true);
                        	msg.setData(bundle);
                        	handler.sendMessage(msg);
                        	
                        	//showErro("Erro na gravação de dados","Gravação NF",false);
                        }
                        else if(palavra.compareTo("Falha GIN")==0)
                        {
                        	gravacao = false;
                        	Message msg = handler.obtainMessage();
                        	Bundle bundle = new Bundle();
                        	bundle.putString("cmd", "MSG");
                        	bundle.putString("msg", "Erro na gravação de dados");
                        	bundle.putString("title", "Gravação NF");
                        	bundle.putBoolean("FIM", true);
                        	msg.setData(bundle);
                        	handler.sendMessage(msg);
                        	
                        	//showErro("Erro na gravação de dados","Gravação NF",false);
                        }
                        else if(palavra.compareTo("Falha CABNF")==0)
                        {
                        	gravacao = false;
                        	Message msg = handler.obtainMessage();
                        	Bundle bundle = new Bundle();
                        	bundle.putString("cmd", "MSG");
                        	bundle.putString("msg", "Erro na gravação do cabeçalho da NF");
                        	bundle.putString("title", "Gravação NF");
                        	bundle.putBoolean("FIM", true);
                        	msg.setData(bundle);
                        	handler.sendMessage(msg);
                        	
                        	//showErro("Erro na gravação de dados","Gravação NF",false);
                        }
                    }
                }
            }catch(Exception e){
            	Message msg = handler.obtainMessage();
            	Bundle bundle = new Bundle();
            	bundle.putString("cmd", "MSG");
            	bundle.putString("msg", e.toString());
            	bundle.putString("title", "Gravação NF");
            	bundle.putBoolean("FIM", true);
            	msg.setData(bundle);
            	handler.sendMessage(msg);
                //Toast.makeText(ConferenciaNF.this, e.toString(), 2).show();
            }
        
            //aguarde_end();
        }
        
        public void receber(){
            try{
                //aguarde_init();
                if(this.server != null)
                {
                    entrada = server.getInputStream();
                    saida = server.getOutputStream();
                    if(this.entrada != null)
                    {
                        String palavra="";
                        palavra = recebeCaracteres();
                        palavra.replace("\n","");
                        if(palavra.compareTo("Encontrado")==0)
                        {
                        	palavra = recebeCaracteres();
                        	if(palavra.compareTo("Falha") !=0)
                        	{
	                        	aguarde_end();
	                            palavra.replace("\n","");
	                            String[] dados = palavra.split(";");
	                            txtForn.setText(dados[1]);
	                            codFornecedor = dados[0];
	                            txtForn.setEnabled(false);
	                            inserir = true;
	                            botaoInserir.setEnabled(inserir);
	                            botaoPesquisar.setEnabled(inserir);
	                            botaoGravar.setEnabled(inserir);
	                            botaoEditar.setEnabled(inserir);
	                            botaoRemover.setEnabled(inserir);
	                            botaoLimpar.setEnabled(inserir);
	                            if(txtChaveAcesso.getText().toString().compareTo("")!= 0)
	                            {
	                            	txtDataEmissao.setFocusableInTouchMode(true);
	                            	txtDataEmissao.setFocusable(true);
	                            	txtDataEmissao.requestFocus();
	                            }
	                            else
	                            {
	                            	txtChaveAcesso.setFocusable(false);
	                            	txtChaveAcesso.setFocusableInTouchMode(false);
	                            	txtNumNF.setFocusableInTouchMode(true);
	                            	txtNumNF.setFocusable(true);
		                            txtNumNF.requestFocus();
	                            }
	                            
                        	}
                        	else
                        	{
                        		showErro("Fornecedor não Encontrado",false);
                        		txtChaveAcesso.setFocusableInTouchMode(true);
                        		txtChaveAcesso.setFocusable(true);
                        		txtCnpj.setFocusableInTouchMode(true);
                        		txtCnpj.setFocusable(true);
                        	}
                        	
                        }
                        else if(palavra.compareTo("CIP")== 0)
                        {
                        	palavra = recebeCaracteres();
                        	if(palavra.compareTo("Encontrado")==0)
                            {
                                palavra = recebeCaracteres();
                                if(palavra.compareTo("Falha") != 0)
                                {
                                        aguarde_end();
                                        palavra.replace("\n","");
                                        String[] dados = palavra.split(";");
                                        quantidadeItem(dados[1],dados[0]);
                                }
                                else
                                {
                                	showErro("Produto não Encontrado",false);
                                }
                            }
                        	else
                        	{
                        		showErro("Produto não Encontrado",false);
                        	}
                        }
                        else if(palavra.compareTo("Gravacao OK")==0)
                        {
                        	gravacao = true;
                        }
                        else if(palavra.compareTo("GINF")==0)
                        {
                        	showErro("Gravação de dados da NF concluida.","Gravação NF",false);
                        	
                        }
                        else if(palavra.compareTo("Falha GIN")==0)
                        {
                        	gravacao = false;
                        	showErro("Erro na gravação de dados","Gravação NF",false);
                        }
                        else if(palavra.compareTo("Nao Encontrado")== 0)
                        {
                        	showErro("Fornecedor não Encontrado",false);
                        	txtForn.setText("");
                        	txtCnpj.setText("");
                        	txtCnpj.setFocusableInTouchMode(true);
                        	txtCnpj.setFocusable(true);
                    		txtCnpj.requestFocus();
                    		
                        }

                    }
                }
            }catch(Exception e){
                Toast.makeText(ConferenciaNF.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        
            //aguarde_end();
        }
        
        public void showErro(String msg,final Boolean exit)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            	.setTitle("Conferencia NF")
        		.setMessage(msg)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	if(exit)
                        	System.exit(0);
                    	
                    }
                })
                .show();
        }
        
        public void showErro(String msg,String titulo,final Boolean exit)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
            	.setTitle(titulo)
        		.setMessage(msg)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            if(exit)
                            	System.exit(0);
                            
                    }
                })
                .show();
        }
        
        /*public void receber(){
            try{
                aguarde_init();
                Thread.sleep(1000);
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(txtCnpj.getWindowToken(), 0);
                if(this.server != null)
                {
                    entrada = server.getInputStream();
                    saida = server.getOutputStream();
                    if(this.entrada != null)
                    {
                        int res;
                        Boolean recebendo = true;
                        String palavra="";
                        palavra = recebeCaracteres();
                        while(recebendo)
                        {
                            res = entrada.read();
                            if((char)res != '\n' & res != -1)
                            {
                                palavra += (char)res;
                            }
                            else if (res == -1)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder
                                	.setTitle("Conferencia NF")
                            		.setMessage("Fornecedor não Encontrado")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                                System.exit(0);
                                        }
                                    })
                                    .show();
                                recebendo = false;
                            }
                            else
                            {
                                palavra.replace("\n","");
                                if(palavra.compareTo("Encontrado")==0)
                                {
                                    palavra = "";
                                    while(recebendo)
                                    {
                                        res = entrada.read();
                                        if((char)res != '\n')
                                        {
                                            palavra += (char)res;
                                        }
                                        else if(res == -1)
                                        {
                                        	recebendo = false;
                                        }
                                        else
                                        {
                                            aguarde_end();
                                            palavra.replace("\n","");
                                            String[] dados = palavra.split(";");
                                            recebendo = false;
                                            txtForn.setText(dados[0]);
                                            txtForn.setEnabled(false);
                                            inserir = true;
                                            botaoInserir.setEnabled(inserir);
                                        }
                                    }
                                }
                                else if(palavra.compareTo("CIP")==0)
                                {
                                	palavra = "";
                                    while(recebendo)
                                    {
                                        res = entrada.read();
                                        if((char)res != '\n')
                                        {
                                            palavra += (char)res;
                                        }
                                        else if(res == -1)
                                        {
                                        	recebendo = false;
                                        }
                                        else
                                        {
		                                	palavra.replace("\n","");
		                                    if(palavra.compareTo("Encontrado")==0)
		                                    {
		                                        palavra = "";
		                                        while(recebendo)
		                                        {
		                                            res = entrada.read();
		                                            if((char)res != '\n')
		                                            {
		                                                palavra += (char)res;
		                                            }
		                                            else if(res == -1)
		                                            {
		                                            	recebendo = false;
		                                            }
		                                            else
		                                            {
		                                                aguarde_end();
		                                                palavra.replace("\n","");
		                                                String[] dados = palavra.split(";");
		                                                recebendo = false;
		                                                quantidadeItem(dados[1],dados[0]);
		                                            }
		                                        }
		                                    }
		                                    else
		                                    {
		                                    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		                                        builder
		                                                .setTitle("Conferencia NF")
		                                                .setMessage("Produto não Encontrado")
		                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
		                                                    public void onClick(DialogInterface dialog, int which) {
		                                                    	listaProd.setFocusable(true);
		                                                    }
		                                                })
		                                                .show();
		                                        recebendo = false;
		                                    }
                                        }
                                    }
                                }
                                else
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                    builder
                                            .setTitle("Conferencia NF")
                                            .setMessage("Fornecedor não Encontrado")
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                	txtCnpj.selectAll();
                                                }
                                            })
                                            .show();
                                    recebendo = false;
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                Toast.makeText(ConferenciaNF.this, e.toString(), 2).show();
            }
        
            aguarde_end();
        }*/

        public String recebeCaracteres() throws Exception
        {
        	int res = -1;
        	Boolean recebendo = true;
            String palavra="";
            while(recebendo)
            {
                res = entrada.read();
                if((char)res != '\n' & res != -1)
                {
                    palavra += (char)res;
                }
                else if (res == -1)
                {
                	recebendo = false;
                	palavra =  "Falha";
                }
                else
                {
                	recebendo = false;
                }
            }
            return palavra;
        }
        
        public void editarQuantidadeItem(String cod,String desc,int pos)
        {
        	final String descri = desc;
        	final String codigo = cod;
        	final int index = pos;
        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
        	
			builder.setTitle("Quantidade do Produto: " + desc);
			
			insert = false;
			// Set up the input
			final EditText input = new EditText(ConferenciaNF.this);
			
			input.setInputType(InputType.TYPE_CLASS_NUMBER);

			builder.setView(input);

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			    }
			});
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	if(insert == false)
			    	{
				    	insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						codProduto.set(index,codigo);
						descProduto.set(index,descri);
						quantProtudo.set(index,input.getText().toString());
						itens.set(index,codigo + " - " + descri + " - " + input.getText().toString() );
	                    adapter = null;
	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	                    listaProd.setAdapter(adapter);
						dialog.cancel();
			    	}
			    }
			});

			final AlertDialog alert = builder.create();
			
			input.setOnKeyListener(new View.OnKeyListener() {
			
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
					if (keyCode == 66 && insert == false && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getRepeatCount() ==0) 
					{
						insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						codProduto.set(index,codigo);
						descProduto.set(index,descri);
						quantProtudo.set(index,input.getText().toString());
						itens.set(index,codigo + " - " + descri + " - " + input.getText().toString() );
	                    adapter = null;
	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	                    listaProd.setAdapter(adapter);
	                    alert.cancel();
					}
					return false;
				}
			});
			
			alert.show();
			
			
			//builder.show();
			
        }
        
        public void inserirCodigo()
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
			
			
			insert = false;
			// Set up the input
			final EditText input = new EditText(ConferenciaNF.this);
			
			builder.setTitle("Código do Produto");
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			builder.setView(input);
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			        ContinuaInserir = false;
			    }
			});
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	if (insert == false) 
					{
						insert = true;
						inserirProduto(input.getText().toString());
						dialog.cancel();
						ContinuaInserir = true;
					}
			    	
			    }
			});
			final AlertDialog alert = builder.create();
			alert.show();
			
			input.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View arg0, int keyCode, KeyEvent keyEvent) {
					if (keyCode == 66 && insert == false && keyEvent.getAction() == KeyEvent.ACTION_UP)
					{
						insert = true;
						inserirProduto(input.getText().toString());
						alert.dismiss();
						
					}
					return false;
				}
			});
        }
        
        public void quantidadeItem(String desc,String cod)
        {
        	final String codigo = cod;
        	final String descri = desc;
        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
        	
			builder.setTitle("Quantidade do Produto: " + desc);
			
			insert = false;
			// Set up the input
			final EditText input = new EditText(ConferenciaNF.this);
			
			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL );

			builder.setView(input);

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			        ContinuaInserir = false;
			    }
			});
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	if(insert == false && input.getText().toString().compareTo("") !=0)
			    	{
				    	insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						codProduto.add(codigo);
						descProduto.add(descri);
						quantProtudo.add(input.getText().toString());
						itens.add(codigo + " - " + descri + " - " + input.getText().toString() );
	                    adapter = null;
	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	                    listaProd.setAdapter(adapter);
						dialog.cancel();
						if(ContinuaInserir)
						{
							inserirCodigo();
						}
			    	}
			    }
			});

			final AlertDialog alert = builder.create();
			
			input.setOnKeyListener(new View.OnKeyListener() {
			
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
					if (keyCode == 66 && insert == false && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getRepeatCount() ==0 && input.getText().toString().compareTo("") !=0) 
					{
						insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						codProduto.add(codigo);
						descProduto.add(descri);
						quantProtudo.add(input.getText().toString());
						itens.add(codigo + " - " + descri + " - " + input.getText().toString() );
	                    adapter = null;
	                    adapter = new ArrayAdapter<String>(ConferenciaNF.this,android.R.layout.simple_list_item_1, itens);
	                    listaProd.setAdapter(adapter);
						alert.cancel();
						if(ContinuaInserir)
						{
							inserirCodigo();
						}
					}
					return false;
				}
			});
			
			alert.show();
			
			
			//builder.show();
			
        }
        
        public void aguarde_init(){
            // prepare for a progress bar dialog

            progressBar.show();
            
            //reset progress bar status
            progressBarStatus = true;
            new Thread(new Runnable() {
                public void run() {
                    while (progressBarStatus==true) {
                        // your computer is too fast, sleep 1 second
                        //try {
                        //    Thread.sleep(1000);
                        //} catch (InterruptedException e) {
                        //    e.printStackTrace();
                        //}
                        // Update the progress bar
                        progressBarHandler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(0);
                            }
                        });
                    }
                    progressBar.dismiss();
                }
            }).start();

        }
        public void aguarde_end(){
        	//progressBar.dismiss();
            progressBarStatus = false;
        }
        
        @Override
        public void onBackPressed() {
        	// TODO Auto-generated method stub
        	
        	final ConferenciaNF conNf = this;
        	
        	if(!inserir)
        	{
        		Intent intent = new Intent(conNf,MainActivity.class);
        		ConferenciaNF.this.startActivity(intent);
        		conNf.finish();
        	}
        	else
        	{
        	AlertDialog.Builder builder = new AlertDialog.Builder(ConferenciaNF.this);
            builder
            	.setTitle("Sair Programa")
        		.setMessage("Deseja mesmo sair ?")
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	inserir = false;
                    	Intent intent = new Intent(conNf,MainActivity.class);
                    	ConferenciaNF.this.startActivity(intent);
                    	conNf.finish();
                    }
                })
                .show();
        	}
        }

}
