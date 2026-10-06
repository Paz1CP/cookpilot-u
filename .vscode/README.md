# Ejecutar desde VS Code

1. Abre la carpeta raiz `cookpilot-u` en VS Code.
2. Instala la extension recomendada **Android** (`adelphes.android-dev-ext`).
3. Conecta un dispositivo con depuracion USB o inicia un emulador. Debe aparecer en `adb devices`.
4. En **Run and Debug** (`Ctrl+Shift+D`), selecciona **CookPilot U: ejecutar y depurar Android**.
5. Pulsa el boton de play o **F5** y selecciona el dispositivo.

VS Code compila `:app:assembleDebug`, instala el APK e inicia la actividad de entrada declarada en el manifiesto (`SplashActivity`) con el debugger. Puedes poner breakpoints en los archivos Java, inspeccionar variables y avanzar paso a paso.

Este proyecto es Android nativo: la ejecucion se inicia desde Run and Debug/F5; no hay un `main.dart` ni un enlace Run/Debug sobre un metodo `main()`.

La tarea usa el JDK de `JAVA_HOME` o `java` disponible en PATH. El SDK debe estar configurado en `local.properties` y `adb` debe estar disponible en PATH. No guardes rutas personales en los archivos compartidos de VS Code.

Si acabas de instalar la extension y VS Code no reconoce el debugger Android, ejecuta **Developer: Reload Window**. Para ver logs, usa **Android: View Logcat** desde la paleta de comandos.
