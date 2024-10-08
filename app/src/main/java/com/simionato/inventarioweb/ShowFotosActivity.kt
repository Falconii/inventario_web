package com.simionato.inventarioweb

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.simionato.inventarioweb.adapters.FotoAdapter
import com.simionato.inventarioweb.databinding.ActivityShowFotosBinding
import com.simionato.inventarioweb.global.CadastrosAcoes
import com.simionato.inventarioweb.global.ParametroGlobal
import com.simionato.inventarioweb.infra.InfraHelper
import com.simionato.inventarioweb.models.FotoModel
import com.simionato.inventarioweb.models.ImobilizadoinventarioModel
import com.simionato.inventarioweb.parametros.ParametroFoto01
import com.simionato.inventarioweb.services.FotoService
import com.simionato.inventarioweb.shared.HttpErrorMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowFotosActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityShowFotosBinding.inflate(layoutInflater)
    }

    private val adapter = FotoAdapter(){foto,idAcao ->

        if (idAcao == CadastrosAcoes.Consulta) {
            chamaFotoWeb(foto)
        }
        if (idAcao == CadastrosAcoes.Exclusao) {
            showDialogDelete(foto)
            /*
            if (foto.id_pasta == "1eQuwNcfTmpYUWUIvlGBouodico8WrjoD") {
                showDialogDelete(foto)
            } else {
                showToast("Não Posso Apagar ESta Foto!")
            }
             */
        }
        if (idAcao == CadastrosAcoes.Edicao) {
            chamaFotoEdicao(foto)
        }
    }

    private lateinit var  dialogDelete: AlertDialog

    private lateinit var  imoinventario: ImobilizadoinventarioModel

    private var params:ParametroFoto01 = ParametroFoto01()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //Validando Paramentros
        if ((ParametroGlobal.Dados.usuario.id == 0) ||
            (ParametroGlobal.Dados.local.id == 0)   ||
            (ParametroGlobal.Dados.Inventario.codigo == 0)){
            showToast("Ambiente Incorreto!!")
            finish()
        }

        try {
            val bundle = intent.extras
            Log.i("zyzz", "Chegue na showfotos ${bundle}")
            if (bundle != null) {
                imoinventario = if (Build.VERSION.SDK_INT >= 33) bundle.getParcelable(
                    "ImoInventario",
                    ImobilizadoinventarioModel::class.java
                )!!
                else bundle.getParcelable("ImoInventario")!!
            } else {
                showToast("Parâmetro Do Inventário Incorreto!!")
                finish()
            }
        } catch (error:Exception){
            showToast("Erro Nos Parametros: ${error.message}")
            finish()
        }

        iniciar()
        /*
        try {
            val url =
                URL("https://drive.google.com/uc?export=view&id=1QSfT3hO40mN-9znlqq7hmdHWQRBe2kRi")
            Glide.with(this)
                .load(url)
                .centerCrop()
                .into(binding.imFoto30)
            getFotos()

        } catch (e:Exception){
            Log.i("zyzz","Erro-> ${e.message}")
        }
*/
    }

    private fun iniciar(){
        inicializarTooBar()
        /*"Local: ${ParametroGlobal.Dados.Inventario.local_razao}\nInventário: ${ParametroGlobal.Dados.Inventario.descricao}\nPlaqueta: ${imoinventario.id_imobilizado}\nDescricao: ${imoinventario.imo_descricao}"*/
        binding.txtViewSituacao30.setText(ParametroGlobal.prettyText.ambiente_produto(imoinventario.id_imobilizado,imoinventario.imo_descricao))

        getFotos()
    }

    private fun inicializarTooBar(){
        binding.ToolBar30.title = "Controle De Ativos"
        binding.ToolBar30.subtitle = ParametroGlobal.Dados.Inventario.descricao
        binding.ToolBar30.setTitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar30.setSubtitleTextColor(
            ContextCompat.getColor(this,R.color.white)
        )
        binding.ToolBar30.inflateMenu(R.menu.menu_show_fotos)
        binding.ToolBar30.setOnMenuItemClickListener { menuItem ->
            when( menuItem.itemId ){

                R.id.menu_show_fotos_refresh -> {
                    getFotos()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_show_fotos_exit -> {
                    val returnIntent: Intent = Intent()
                    setResult(Activity.RESULT_CANCELED,returnIntent)
                    finish()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_show_fotos_new -> {
                    chamaFoto()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }
    private fun getFotos(){
        try {
            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )
            params.id_empresa       = imoinventario.id_empresa
            params.id_local         = imoinventario.id_filial
            params.id_inventario    = imoinventario.id_inventario
            params.id_imobilizado   = imoinventario.id_imobilizado
            params.destaque = ""
            binding.llProgress30.visibility = View.VISIBLE
            fotoService.getFotos(params).enqueue(object :
                Callback<List<FotoModel>> {
                override fun onResponse(
                    call: Call<List<FotoModel>>,
                    response: Response<List<FotoModel>>
                ) {
                    binding.llProgress30.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var fotos = response.body()

                            if (fotos !== null) {

                                montaLista(fotos);

                            } else {
                                showToast("Falha No Retorno Da Requisição!")
                            }
                        }
                        else {
                            binding.llProgress30.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Tabela De Fotos Vazia")
                                val fotos:List<FotoModel> = listOf()
                                montaLista(fotos )
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress30.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<List<FotoModel>>, t: Throwable) {
                    binding.llProgress30.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

        }catch (e: Exception){
            binding.llProgress30.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }

    private fun deleteFoto(foto:FotoModel){
        try {
            val fotoService = InfraHelper.apiInventario.create( FotoService::class.java )

            fotoService.DeleteFoto(foto).enqueue( object  : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    binding.llProgress30.visibility = View.GONE
                    if (response != null) {
                        if (response.isSuccessful) {

                            var retorno = response.body()

                            if (retorno !== null) {

                                getFotos()

                            } else {

                                showToast("Falha No Retorno Da Requisição!")

                            }

                        }
                        else {
                            binding.llProgress30.visibility = View.GONE
                            val gson = Gson()
                            val message = gson.fromJson(
                                response.errorBody()!!.charStream(),
                                HttpErrorMessage::class.java
                            )
                            if (response.code() == 409){
                                showToast("Tabela De Fotos Vazia")
                                var fotos: List<FotoModel> = listOf()
                                montaLista(fotos)
                            } else {
                                showToast("${message.getMessage().toString()}", Toast.LENGTH_SHORT)
                            }
                        }

                    }
                    else {
                        binding.llProgress30.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    binding.llProgress30.visibility = View.GONE
                    showToast(t.message.toString())
                }
            })

            binding.llProgress30.visibility = View.VISIBLE
        }catch (e: Exception){
            binding.llProgress30.visibility = View.GONE
            showToast("${e.message.toString()}", Toast.LENGTH_LONG)
        }

    }


    private fun chamaFoto(){

        val intent = Intent(this,FotosActivity::class.java)
        intent.putExtra("id_imobilizado",imoinventario.id_imobilizado)
        intent.putExtra("descricao",imoinventario.imo_descricao)
        getRetornoFoto.launch(intent)
    }

    private val getRetornoFoto =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                getFotos()
            } 
        }

    fun chamaFotoWeb(foto:FotoModel){
        val intent = Intent(this,FotoWebActivity::class.java)
        intent.putExtra("id_file",foto.id_file)
        startActivity(intent)
    }


    private fun chamaFotoEdicao(foto:FotoModel){

        val intent = Intent(this,EditFotoActivity::class.java)
        intent.putExtra("foto",foto)
        getRetornoFoto.launch(intent)
    }

    private val getRetornoFotoEdicao =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                getFotos()
            }
        }


    private fun showDialogDelete(foto:FotoModel){
        val builder = AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage("Confirma A exclusão Da Foto ?")
            .setNegativeButton("Não"){_,_  -> dialogDelete.dismiss()
            }
            .setPositiveButton("Sim"){_,_ ->
                deleteFoto(foto)
            }
        dialogDelete = builder.create()

        dialogDelete.show()
    }

    private fun  montaLista(fotos:List<FotoModel>){
        adapter.lista = fotos
        binding.rvLista30.adapter = adapter
        binding.rvLista30.layoutManager =
            LinearLayoutManager(binding.rvLista30.context)
        binding.rvLista30.addItemDecoration(
            DividerItemDecoration(
                binding.rvLista30.context,
                RecyclerView.VERTICAL
            )
        )
    }
    fun showToast(mensagem:String,duracao:Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, mensagem, duracao).show()
    }
}