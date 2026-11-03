package com.oratoriamaster.ia;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private final String[] titles = {"Dicción y pronunciación","Control de muletillas","Respiración y pausas","Persuasión","Improvisación","Debate y argumentos","Narración de historias","Presentaciones públicas","Oratoria jurídica","Preguntas difíciles","Entonación y ritmo","Confianza escénica"};
    private final String[] prompts = {
        "Lee un trabalenguas despacio, articula cada sílaba y repítelo con naturalidad.",
        "Explica tu actividad favorita durante un minuto sin decir eh, este o pues.",
        "Explica una idea en tres partes. Haz una pausa breve entre cada parte.",
        "Convence a un público de que la lectura diaria es importante. Ofrece dos razones.",
        "Improvisa un discurso sobre un objeto que tengas cerca, con inicio y conclusión.",
        "Defiende una propuesta educativa con una tesis, dos argumentos y un contraargumento.",
        "Cuenta una historia con protagonista, conflicto y desenlace.",
        "Preséntate ante una audiencia imaginaria y comunica una idea principal.",
        "Expón un alegato breve sobre un caso completamente ficticio, con hechos y conclusión.",
        "Responde: ¿Qué harías si alguien cuestiona tu propuesta? Argumenta con respeto.",
        "Lee un texto cambiando entonación, velocidad y pausas para destacar ideas.",
        "Da un discurso de agradecimiento de un minuto con postura segura."
    };
    private final int NAVY=Color.rgb(18,39,80), BLUE=Color.rgb(43,108,224), PALE=Color.rgb(241,246,255);
    private LinearLayout body;
    private TextView clock;
    private int selected=0, seconds=0;
    private boolean running=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private static final int SPEECH=77;

    @Override public void onCreate(Bundle state){super.onCreate(state);home();}
    private GradientDrawable background(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    private void screen(String heading,String subtitle){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(24,34,24,32);body.setBackgroundColor(PALE);
        scroll.addView(body);setContentView(scroll);
        label("ORATORIA MASTER IA",14,BLUE,true);
        label(heading,28,NAVY,true);
        label(subtitle,15,Color.DKGRAY,false);
    }
    private TextView label(String text,int size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);
        v.setPadding(8,9,8,9);if(bold)v.setTypeface(null,Typeface.BOLD);body.addView(v);return v;
    }
    private void action(String title,Runnable fn){
        Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(Color.WHITE);
        b.setBackground(background(BLUE,20));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,8,0,8);
        body.addView(b,lp);b.setOnClickListener(v->fn.run());
    }
    private void home(){
        running=false;
        screen("Habla con seguridad","Tu entrenador personal de comunicación. Practica a tu ritmo.");
        int completed=getPreferences(0).getInt("completed",0);
        label("Prácticas completadas: "+completed,20,NAVY,true);
        label("12 habilidades • Ejercicios guiados • Cronómetro • Análisis básico de texto",16,NAVY,false);
        action("Comenzar entrenamiento",()->catalog());
        action("Ver mi progreso",()->progress());
        label("Puedes utilizar los ejercicios sin conexión. El dictado depende del servicio de voz disponible en tu teléfono.",13,Color.DKGRAY,false);
    }
    private void catalog(){
        running=false;screen("Elige una habilidad","Selecciona un módulo y practica durante 1 a 3 minutos.");
        for(int i=0;i<titles.length;i++){final int index=i;action((i+1)+". "+titles[i],()->{selected=index;practice();});}
        action("Volver al inicio",()->home());
    }
    private void practice(){
        running=false;seconds=0;
        screen(titles[selected],"Ejercicio "+(selected+1)+" de 12");
        label("TU RETO",13,BLUE,true);label(prompts[selected],19,NAVY,false);
        clock=label("00:00",34,NAVY,true);
        action("Iniciar / pausar cronómetro",()->{running=!running;if(running)tick();});
        action("Reiniciar cronómetro",()->{running=false;seconds=0;clock.setText("00:00");});
        action("Hablar y transcribir",()->listen());
        action("Marcar práctica completada",()->{
            getPreferences(0).edit().putInt("completed",getPreferences(0).getInt("completed",0)+1)
              .putInt("last",selected).apply();running=false;progress();
        });
        action("Elegir otra habilidad",()->catalog());
    }
    private void tick(){
        handler.postDelayed(()->{
            if(running && clock!=null){seconds++;clock.setText(String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60));tick();}
        },1000);
    }
    private void listen(){
        try{
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-EC");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla sobre el ejercicio de oratoria");
            startActivityForResult(i,SPEECH);
        }catch(Exception e){new AlertDialog.Builder(this).setMessage("El reconocimiento de voz no está disponible. Puedes continuar con el ejercicio y el cronómetro.").setPositiveButton("Aceptar",null).show();}
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req!=SPEECH||result!=RESULT_OK||data==null)return;
        ArrayList<String> candidates=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if(candidates==null||candidates.isEmpty())return;
        running=false;
        String transcript=candidates.get(0).trim();
        String[] words=transcript.isEmpty()?new String[0]:transcript.split("\\s+");
        int fillers=0;
        for(String word:words){String normalized=word.toLowerCase(Locale.ROOT).replaceAll("[^a-záéíóúüñ]","");
            if(normalized.equals("eh")||normalized.equals("este")||normalized.equals("mmm"))fillers++;
        }
        screen("Tu evaluación","Análisis orientativo de la transcripción, no de la calidad acústica.");
        label("Palabras reconocidas: "+words.length,19,NAVY,true);
        label("Posibles muletillas: "+fillers,19,NAVY,true);
        if(seconds>0)label("Ritmo aproximado: "+Math.round(words.length*60f/seconds)+" palabras/minuto",18,NAVY,false);
        label("TRANSCRIPCIÓN",14,BLUE,true);label(transcript,16,NAVY,false);
        label("Consejo: comunica una idea principal, añade ejemplos y termina con una conclusión. El reconocimiento puede omitir palabras o muletillas.",15,Color.DKGRAY,false);
        action("Volver al ejercicio",()->practice());
        action("Ver otras habilidades",()->catalog());
    }
    private void progress(){
        running=false;screen("Mi progreso","Tus resultados se guardan localmente en este teléfono.");
        label("Prácticas completadas: "+getPreferences(0).getInt("completed",0),24,NAVY,true);
        int last=getPreferences(0).getInt("last",-1);
        label("Último módulo: "+(last<0?"Todavía ninguno":titles[last]),17,NAVY,false);
        action("Continuar entrenando",()->catalog());
        action("Inicio",()->home());
    }
    @Override public void onBackPressed(){running=false;home();}
}