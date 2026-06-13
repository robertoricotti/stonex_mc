Modifiche applicate a stx_hole_converter.py
=========================================

1) Nuova modalità UI: AUTO / GENERIC CSV
   - Drag&drop o selezione file senza dover scegliere per forza ROCK/JET/SOLAR.
   - I template noti continuano a essere riconosciuti dai loro header.

2) CSV generici supportati
   - Header opzionale.
   - Delimitatori: virgola, punto e virgola, tab.
   - Supporto righe completamente quotate, per esempio: "NUMBER;X;Y;Altezza palo".
   - Formati senza header: PENZD, PNEZD, ENZD, NEZD.
   - In AUTO prova a distinguere E/N e N/E con euristica; se ambiguo assume PENZD.

3) Regole richieste
   - P mancante: numerazione progressiva 1, 2, 3, ...
   - Z mancante o vuota: 0.000
   - D mancante o vuota: Comment vuoto
   - Se esiste solo StartPoint, viene creato EndPoint verticale.
   - Distanza positiva: End Z più in basso, quindi Z_end = Z_start - distanza.
   - Distanza negativa: End Z più in alto.

4) Unità di misura per la distanza Start-End
   - m (default)
   - usFeet
   - UsSurveyFeet

Nota: il file stonex_converter.exe in dist/ NON è stato ricompilato qui. Per aggiornare l'eseguibile usare PyInstaller sul nuovo stx_hole_converter.py.
