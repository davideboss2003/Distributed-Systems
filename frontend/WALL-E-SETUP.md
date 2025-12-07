# Wall-E Mascot Setup

## 📹 Video Setup

Pentru a activa mascota Wall-E în colțul din dreapta jos al dashboard-ului, trebuie să adaugi videoclipul generat cu Veo3 de la Google.

### Pași:

1. **Descarcă videoclipul Wall-E.mp4** generat cu Veo3
2. **Copiază fișierul** în folderul `frontend/public/`
3. **Numele fișierului** trebuie să fie exact: `Wall-e.mp4`

### Structura finală:

```
frontend/
  public/
    Wall-e.mp4  ← Adaugă videoclipul aici
    avatar.png
    placeholder-logo.png
    ...
```

### Funcționalitate:

- **Buton mascot** apare în colțul din dreapta jos pe toate paginile dashboard-ului
- **Click pe mascot** → se deschide videoclipul în modal fullscreen
- **Animație** → buton cu efect de glow și pulse
- **Auto-close** → videoclipul se închide automat când se termină

### Dacă nu ai videoclipul:

Poți genera unul nou cu Google Veo3 sau poți folosi orice video MP4 cu Wall-E sau alt personaj mascot.

## 🎨 Customizare

Pentru a schimba aspectul mascotei, editează:
- `frontend/components/wall-e-mascot.tsx`

Pentru a schimba poziția:
- Modifică clasele `fixed bottom-6 right-6` în componenta `WallEMascot`
