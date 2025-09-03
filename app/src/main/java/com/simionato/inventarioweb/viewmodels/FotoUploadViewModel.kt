import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simionato.inventarioweb.models.FotoUploadModel
import com.simionato.inventarioweb.repositories.FotoRepository
import com.simionato.inventarioweb.global.ParametroGlobal.*
import kotlinx.coroutines.launch

class FotoViewModel(
    application: Application,
    private val repository: FotoRepository
) : AndroidViewModel(application) {

    fun enviarFoto(foto: FotoUploadModel) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val resultado = repository.uploadFoto(context, foto)

            when (resultado) {
                is EstadoUpload.Sucesso -> {
                    // Atualiza UI, dispara toast, etc.
                }
                is EstadoUpload.Falha -> {
                    Log.e("Upload", resultado.mensagem)
                    // Exibe mensagem de erro, mostra dialog, etc.
                }
                EstadoUpload.Carregando -> TODO()
            }
        }
    }
}