package ui.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import state.getResourcesFile
import ui.window.windowBackgroundFlashingOnCloseFixHack
import java.io.File
import java.util.concurrent.FutureTask
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileSystemView

@Composable
fun BuiltInVocabularyDialog(
    show: Boolean,
    close: () -> Unit,
    openChooseVocabulary: (String) -> Unit = {},
    futureFileChooser: FutureTask<JFileChooser>
) {
    if(show){
        DialogWindow(
            title = "选择内置词库到本地文件系统",
            icon = painterResource("logo/logo.png"),
            onCloseRequest = { close() },
            resizable = false,
            state = rememberDialogState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(940.dp, 700.dp)
            ),
        ) {
            windowBackgroundFlashingOnCloseFixHack()
            Surface(
                elevation = 5.dp,
                shape = RectangleShape,
            ) {
                Box (Modifier.fillMaxSize()
                    .background(color = MaterialTheme.colors.background)

                ){
                    var finish by remember{ mutableStateOf(false)}
                    var waiting by remember{ mutableStateOf(false)}
                    val scope = rememberCoroutineScope()
                    /** 保存词库 */
                    val save:(File) -> Unit = { file ->
                        waiting = true
                        scope.launch(Dispatchers.IO) {
                            var name = file.nameWithoutExtension
                            if (file.parentFile.nameWithoutExtension == "人教版英语" ||
                                file.parentFile.nameWithoutExtension == "外研版英语" ||
                                file.parentFile.nameWithoutExtension == "北师大版高中英语"
                            ) {
                                if (name.contains(" ")) {
                                    name = name.split(" ")[1]
                                }
                            }
                            val fileChooser = futureFileChooser.get()
                            fileChooser.dialogType = JFileChooser.SAVE_DIALOG
                            fileChooser.dialogTitle = "保存词库"
                            val myDocuments = FileSystemView.getFileSystemView().defaultDirectory.path
                            fileChooser.selectedFile = File("$myDocuments${File.separator}${name}.json")
                            val userSelection = fileChooser.showSaveDialog(window)
                            if (userSelection == JFileChooser.APPROVE_OPTION) {

                                val fileToSave = fileChooser.selectedFile
                                if (fileToSave.exists()) {
                                    // 是-0,否-1，取消-2
                                    val answer =
                                        JOptionPane.showConfirmDialog(window, "${name}.json 已存在。\n要替换它吗？")
                                    if (answer == 0) {
                                        try{
                                            fileToSave.writeBytes(file.readBytes())
                                            finish = true
                                            close()
                                            openChooseVocabulary(fileToSave.absolutePath)
                                        }catch (e:Exception){
                                            e.printStackTrace()
                                            JOptionPane.showMessageDialog(window,"保存失败，错误信息：\n${e.message}")
                                        }

                                    }
                                } else {
                                    try{
                                        fileToSave.writeBytes(file.readBytes())
                                        finish = true
                                        close()
                                        openChooseVocabulary(fileToSave.absolutePath)
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                        JOptionPane.showMessageDialog(window,"保存失败，错误信息：\n${e.message}")
                                    }

                                }

                            }
                            waiting = false
                        }
                    }

                    val stateVertical = rememberScrollState(0)
                    Box(Modifier.fillMaxSize().verticalScroll(stateVertical)){
                        Column (Modifier.padding(10.dp)){
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/大学英语"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/出国"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/牛津核心词"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/北师大版高中英语"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/人教版英语"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/外研版英语"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/新概念英语"),
                                save = save
                            )
                            VocabularyCategory(
                                directory = getResourcesFile("vocabulary/商务英语"),
                                save = save
                            )
                        }
                    }
                    if(waiting){
                        CircularProgressIndicator(Modifier.align(Alignment.Center).size(150.dp))
                    }
                    if(finish){
                        Surface (Modifier.size(206.dp,83.dp)
                            .align(Alignment.Center)
                            .border(BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)))
                        ){
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ){
                                Icon(
                                    imageVector = Icons.Outlined.TaskAlt,
                                    contentDescription = "",
                                    tint = Color.Green
                                )
                                Text(
                                    text = "保存成功",
                                    color = MaterialTheme.colors.onBackground,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                            LaunchedEffect(Unit){
                                delay(2000)
                                finish = false
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(stateVertical)
                    )
                    Divider(Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }

}

@Composable
fun VocabularyCategory(
    directory: File,
    save:(File)->Unit
){
    Column (Modifier.fillMaxWidth()
        .heightIn(min = 100.dp,max = 500.dp)
        .padding(bottom = 30.dp)){
        Text(directory.nameWithoutExtension,
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 17.dp,top = 5.dp))
        if(directory.isDirectory && !directory.listFiles().isNullOrEmpty() ){
            val files = directory.listFiles()
            if(directory.nameWithoutExtension == "人教版英语" ||
                directory.nameWithoutExtension == "外研版英语" ||
                directory.nameWithoutExtension == "北师大版高中英语"){
                files.sortBy{it.nameWithoutExtension.split(" ")[0].toFloat()}
            }
            val listState = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier.fillMaxWidth(),
                state = listState
            ) {

                    items(files){file ->
                        Card(
                            modifier = Modifier
                                .padding(7.5.dp)
                                .clickable {save(file)},
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 3.dp
                        ) {
                            Box(Modifier.size(width = 160.dp, height = 65.dp)) {
                                var name = file.nameWithoutExtension
                                if(directory.nameWithoutExtension == "人教版英语" ||
                                    directory.nameWithoutExtension == "外研版英语"||
                                    directory.nameWithoutExtension == "北师大版高中英语"){
                                    if(name.contains(" ")){
                                        name = name.split(" ")[1]
                                    }

                                }
                                Text(
                                    text = name,
                                    color = MaterialTheme.colors.onBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
            }
        }

    }
}