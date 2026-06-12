from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor, Color
from reportlab.lib.utils import ImageReader
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_CENTER
import math

W, H = A4  # 595 x 842 pts

# Color palette
BG       = HexColor("#13131A")
CARD     = HexColor("#1C1C24")
BORDER   = HexColor("#2C2C35")
BLUE     = HexColor("#3B82F6")
INDIGO   = HexColor("#6366F1")
PINK     = HexColor("#EC4899")
FUCHSIA  = HexColor("#D946EF")
WHITE    = HexColor("#FAFAFA")
GRAY     = HexColor("#A0A0AB")
GREEN    = HexColor("#10B981")
DARKNAVY = HexColor("#0B0D17")
PURPLE   = HexColor("#5B21B6")

LOGO = r"C:\Users\bms16\.gemini\antigravity\scratch\swart-frontend-android\app\src\main\res\drawable-mdpi\dise_o_de_la_ui_logotipo__1_.png"

def hex_to_rgb01(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16)/255 for i in (0, 2, 4))

def draw_rounded_rect(c, x, y, w, h, r, fill=None, stroke=None, stroke_width=1):
    c.saveState()
    if fill:
        c.setFillColor(fill)
    if stroke:
        c.setStrokeColor(stroke)
        c.setLineWidth(stroke_width)
    p = c.beginPath()
    p.moveTo(x + r, y)
    p.lineTo(x + w - r, y)
    p.arcTo(x + w - 2*r, y, x + w, y + 2*r, startAng=-90, extent=90)
    p.lineTo(x + w, y + h - r)
    p.arcTo(x + w - 2*r, y + h - 2*r, x + w, y + h, startAng=0, extent=90)
    p.lineTo(x + r, y + h)
    p.arcTo(x, y + h - 2*r, x + 2*r, y + h, startAng=90, extent=90)
    p.lineTo(x, y + r)
    p.arcTo(x, y, x + 2*r, y + 2*r, startAng=180, extent=90)
    p.close()
    c.drawPath(p, fill=1 if fill else 0, stroke=1 if stroke else 0)
    c.restoreState()

def draw_gradient_rect(c, x, y, w, h, color1, color2, steps=40):
    r1, g1, b1 = hex_to_rgb01(color1) if isinstance(color1, str) else (color1.red, color1.green, color1.blue)
    r2, g2, b2 = hex_to_rgb01(color2) if isinstance(color2, str) else (color2.red, color2.green, color2.blue)
    for i in range(steps):
        t = i / steps
        rc = r1 + (r2 - r1)*t
        gc = g1 + (g2 - g1)*t
        bc = b1 + (b2 - b1)*t
        c.setFillColor(Color(rc, gc, bc))
        c.rect(x, y + i*(h/steps), w, h/steps + 1, fill=1, stroke=0)

def draw_phone(c, cx, cy, pw, ph):
    """Draw a phone frame centered at (cx, cy)"""
    px, py = cx - pw/2, cy - ph/2
    r = pw * 0.12
    # Shadow
    c.saveState()
    c.setFillColor(Color(0, 0, 0, 0.4))
    draw_rounded_rect(c, px+4, py-4, pw, ph, r, fill=Color(0,0,0,0.4))
    c.restoreState()
    # Body
    draw_rounded_rect(c, px, py, pw, ph, r, fill=CARD, stroke=BORDER, stroke_width=1.5)
    # Notch
    notch_w, notch_h = pw*0.35, ph*0.025
    c.setFillColor(BG)
    draw_rounded_rect(c, cx - notch_w/2, py + ph - notch_h - 2, notch_w, notch_h, 4, fill=BG)
    # Screen area
    screen_margin = pw * 0.05
    screen_top_margin = notch_h + 6
    sx = px + screen_margin
    sy = py + screen_margin
    sw = pw - 2*screen_margin
    sh = ph - screen_margin - screen_top_margin - 4
    return sx, sy, sw, sh

def draw_map_screen(c, sx, sy, sw, sh):
    """Draw a simplified map screen"""
    # Map background
    draw_rounded_rect(c, sx, sy, sw, sh, 4, fill=HexColor("#1c1c24"))

    # Grid lines (streets)
    c.saveState()
    c.setStrokeColor(HexColor("#2c2c35"))
    c.setLineWidth(0.5)
    for i in range(8):
        yy = sy + (sh / 8) * i
        c.line(sx, yy, sx+sw, yy)
    for i in range(6):
        xx = sx + (sw / 6) * i
        c.line(xx, sy, xx, sy+sh)
    c.restoreState()

    # Diagonal avenue
    c.saveState()
    c.setStrokeColor(HexColor("#3b3b45"))
    c.setLineWidth(2)
    c.line(sx, sy + sh*0.7, sx+sw, sy + sh*0.15)
    c.restoreState()

    # Park block
    c.setFillColor(HexColor("#1a2e1a"))
    c.rect(sx + sw*0.55, sy + sh*0.45, sw*0.3, sh*0.2, fill=1, stroke=0)

    # Map pins (exhibition markers)
    pins = [
        (sx + sw*0.25, sy + sh*0.6, BLUE),
        (sx + sw*0.55, sy + sh*0.35, PINK),
        (sx + sw*0.7,  sy + sh*0.65, INDIGO),
        (sx + sw*0.4,  sy + sh*0.5,  FUCHSIA),
    ]
    for px2, py2, col in pins:
        c.setFillColor(Color(col.red, col.green, col.blue, 0.25))
        c.circle(px2, py2, 8, fill=1, stroke=0)
        c.setFillColor(col)
        c.circle(px2, py2, 4, fill=1, stroke=0)
        c.setFillColor(WHITE)
        c.circle(px2, py2, 1.5, fill=1, stroke=0)

    # Search bar at top
    bar_h = sh * 0.1
    bar_y = sy + sh - bar_h - 4
    draw_rounded_rect(c, sx+2, bar_y, sw-4, bar_h, 6, fill=HexColor("#1C1C24"), stroke=BORDER, stroke_width=0.8)
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 5.5)
    c.drawString(sx + 10, bar_y + bar_h*0.35, "Buscar exposiciones...")

    # Bottom card
    card_h = sh * 0.22
    draw_rounded_rect(c, sx+2, sy+2, sw-4, card_h, 6, fill=HexColor("#1C1C24"), stroke=BORDER, stroke_width=0.8)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 5.5)
    c.drawString(sx + 8, sy + card_h*0.6, "Galeria Norte")
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 4.5)
    c.drawString(sx + 8, sy + card_h*0.35, "Arte contemporaneo")
    c.setFillColor(GREEN)
    c.circle(sx + sw - 14, sy + card_h*0.5, 3, fill=1, stroke=0)

def draw_swipe_screen(c, sx, sy, sw, sh):
    """Draw a simplified swipe/discovery screen"""
    c.setFillColor(BG)
    draw_rounded_rect(c, sx, sy, sw, sh, 4, fill=BG)

    # Card stack (back)
    card_m = sw * 0.06
    card_w = sw - 2*card_m
    card_h = sh * 0.68
    card_x = sx + card_m
    card_y = sy + sh * 0.17

    # Back card (slightly offset)
    draw_rounded_rect(c, card_x+5, card_y-4, card_w, card_h, 10, fill=CARD)

    # Main swipe card
    draw_rounded_rect(c, card_x, card_y, card_w, card_h, 10, fill=CARD)

    # Artwork image placeholder (gradient)
    img_h = card_h * 0.72
    draw_gradient_rect(c, card_x, card_y + card_h - img_h, card_w, img_h,
                       "#3B82F6", "#EC4899")

    # Abstract art shapes on card
    c.saveState()
    c.setFillColor(Color(1,1,1,0.15))
    c.circle(card_x + card_w*0.3, card_y + card_h - img_h*0.4, card_w*0.18, fill=1, stroke=0)
    c.setFillColor(Color(1,1,1,0.1))
    c.circle(card_x + card_w*0.7, card_y + card_h - img_h*0.6, card_w*0.22, fill=1, stroke=0)
    c.restoreState()

    # Gradient overlay at bottom of image
    c.saveState()
    steps = 12
    for i in range(steps):
        t = i / steps
        alpha = t * 0.9
        c.setFillColor(Color(CARD.red, CARD.green, CARD.blue, alpha))
        band_h = img_h * 0.35 / steps
        c.rect(card_x, card_y + card_h - img_h + i*band_h, card_w, band_h+1, fill=1, stroke=0)
    c.restoreState()

    # Artist info on card
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 6.5)
    c.drawString(card_x + 8, card_y + card_h*0.27, "Elena Vidal")
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 5)
    c.drawString(card_x + 8, card_y + card_h*0.19, "Pintura abstracta")
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 4.5)
    c.drawString(card_x + 8, card_y + card_h*0.11, "Madrid · Hasta 20 Jun")

    # Like/Dislike buttons
    btn_y = sy + sh*0.06
    btn_r = sw * 0.13
    # Dislike
    c.setFillColor(HexColor("#2C2C35"))
    c.circle(card_x + card_w*0.28, btn_y, btn_r, fill=1, stroke=0)
    c.setFillColor(PINK)
    c.setFont("Helvetica-Bold", 8)
    c.drawCentredString(card_x + card_w*0.28, btn_y - 3, "X")
    # Like
    c.setFillColor(BLUE)
    c.circle(card_x + card_w*0.72, btn_y, btn_r, fill=1, stroke=0)
    c.setFillColor(WHITE)
    c.drawCentredString(card_x + card_w*0.72, btn_y - 3, "♥")

    # Top label
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 5.5)
    c.drawCentredString(sx + sw/2, sy + sh*0.95, "Descubrir")

def draw_exhibitions_screen(c, sx, sy, sw, sh):
    """Draw a simplified exhibitions screen"""
    bg = HexColor("#0B0D17")
    draw_rounded_rect(c, sx, sy, sw, sh, 4, fill=bg)

    # Header
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 6.5)
    c.drawString(sx + 8, sy + sh*0.92, "Exposiciones")
    c.setFillColor(HexColor("#8B5CF6"))
    c.setFont("Helvetica", 4.5)
    c.drawString(sx + 8, sy + sh*0.86, "3 activas")

    # Exhibition cards
    cards_data = [
        ("Cromatismos", "Pintura", "#10B981", 0.73),
        ("Formas Vivas", "Escultura", "#3B82F6", 0.49),
        ("Luz y Sombra", "Fotografia", "#8B5CF6", 0.25),
    ]
    card_h = sh * 0.19
    card_m = sw * 0.05
    cw = sw - 2*card_m

    for label, discipline, accent_hex, ypos in cards_data:
        cy2 = sy + sh * ypos
        draw_rounded_rect(c, sx + card_m, cy2, cw, card_h, 6, fill=HexColor("#161925"), stroke=HexColor("#2C2C35"), stroke_width=0.6)

        # Accent stripe
        accent = HexColor(accent_hex)
        draw_rounded_rect(c, sx + card_m, cy2, 3, card_h, 2, fill=accent)

        # Dot status
        c.setFillColor(accent)
        c.circle(sx + card_m + cw - 12, cy2 + card_h*0.55, 3, fill=1, stroke=0)

        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 5.5)
        c.drawString(sx + card_m + 10, cy2 + card_h*0.6, label)
        c.setFillColor(GRAY)
        c.setFont("Helvetica", 4.5)
        c.drawString(sx + card_m + 10, cy2 + card_h*0.3, discipline)

    # Bottom nav
    nav_h = sh * 0.1
    nav_y = sy + 2
    draw_rounded_rect(c, sx, nav_y, sw, nav_h, 4, fill=HexColor("#1C1C24"))
    nav_items = ["🗺", "⭐", "+", "💬", "👤"]
    for i, icon in enumerate(nav_items):
        nx = sx + sw * (i + 0.5) / len(nav_items)
        col = HexColor("#8B5CF6") if i == 1 else GRAY
        c.setFillColor(col)
        c.setFont("Helvetica", 7)
        c.drawCentredString(nx, nav_y + nav_h*0.3, icon)


def make_poster():
    out_path = r"C:\Users\bms16\.gemini\antigravity\scratch\swart-frontend-android\SWART_poster.pdf"
    c = canvas.Canvas(out_path, pagesize=A4)

    # ── BACKGROUND ──
    c.setFillColor(BG)
    c.rect(0, 0, W, H, fill=1, stroke=0)

    # ── TOP GRADIENT BAND ──
    draw_gradient_rect(c, 0, H - 90, W, 90, "#6366F1", "#EC4899")
    c.setFillColor(BG)
    c.rect(0, H - 90, W, 90, fill=1, stroke=0)  # erase; re-draw with alpha sim
    # Subtle top glow
    c.saveState()
    for i in range(20):
        t = i / 20
        alpha = 0.06 * (1 - t)
        c.setFillColor(Color(0.39, 0.51, 0.965, alpha))
        c.rect(0, H - i*4.5, W, 4.5, fill=1, stroke=0)
    c.restoreState()

    # ── LOGO ──
    logo_w, logo_h = 200, 70
    logo_x = W/2 - logo_w/2
    logo_y = H - logo_h - 18
    try:
        c.drawImage(LOGO, logo_x, logo_y, width=logo_w, height=logo_h,
                    preserveAspectRatio=True, mask='auto')
    except Exception:
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 42)
        c.drawCentredString(W/2, H - 58, "SWART")

    # ── TAGLINE ──
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 13.5)
    c.drawCentredString(W/2, H - 105, "Descubre. Conecta. Exponte.")
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 9.5)
    c.drawCentredString(W/2, H - 120,
        "La app que visibiliza las exposiciones de artistas emergentes")

    # ── SEPARATOR LINE ──
    c.saveState()
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.8)
    c.line(40, H - 132, W - 40, H - 132)
    c.restoreState()

    # ── SCREEN LABELS ──
    labels = ["Mapa de exposiciones", "Descubre artistas", "Mis exposiciones"]
    phone_w  = 115
    phone_h  = 200
    gap      = (W - 3*phone_w) / 4
    phones_y = H - 145 - phone_h  # top of phones area

    label_y  = phones_y + phone_h + 10

    for i, lbl in enumerate(labels):
        cx = gap * (i+1) + phone_w * i + phone_w/2
        # Gradient pill
        pill_w = 110
        pill_h = 18
        draw_rounded_rect(c, cx - pill_w/2, label_y, pill_w, pill_h, 9,
                          fill=HexColor("#1C1C24"), stroke=BORDER, stroke_width=0.8)
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 7)
        c.drawCentredString(cx, label_y + 5.5, lbl)

    # ── PHONE MOCKUPS ──
    draw_fns = [draw_map_screen, draw_swipe_screen, draw_exhibitions_screen]
    for i, draw_fn in enumerate(draw_fns):
        cx = gap * (i+1) + phone_w * i + phone_w/2
        cy = phones_y + phone_h/2
        sx, sy, sw2, sh2 = draw_phone(c, cx, cy, phone_w, phone_h)
        draw_fn(c, sx, sy, sw2, sh2)

    # ── CTA SECTION ──
    cta_y = phones_y - 42
    # Gradient pill CTA
    cta_w = 260
    draw_gradient_rect(c, W/2 - cta_w/2, cta_y, cta_w, 30, "#3B82F6", "#EC4899", steps=30)
    draw_rounded_rect(c, W/2 - cta_w/2, cta_y, cta_w, 30, 15,
                      stroke=None)
    # Clip to pill shape by overdrawing corners — simpler: just use rounded rect with fill on top
    c.setFillColor(BG)
    # Re-draw bg corners via clipping is complex; skip, gradient strip looks fine
    c.setFillColor(Color(0.07, 0.07, 0.1, 0.0))

    # Re-draw as proper rounded gradient
    c.saveState()
    p = c.beginPath()
    r2 = 15
    x0, y0 = W/2 - cta_w/2, cta_y
    p.moveTo(x0 + r2, y0)
    p.lineTo(x0 + cta_w - r2, y0)
    p.arcTo(x0 + cta_w - 2*r2, y0, x0 + cta_w, y0 + 2*r2, startAng=-90, extent=90)
    p.lineTo(x0 + cta_w, y0 + 30 - r2)
    p.arcTo(x0 + cta_w - 2*r2, y0 + 30 - 2*r2, x0 + cta_w, y0 + 30, startAng=0, extent=90)
    p.lineTo(x0 + r2, y0 + 30)
    p.arcTo(x0, y0 + 30 - 2*r2, x0 + 2*r2, y0 + 30, startAng=90, extent=90)
    p.lineTo(x0, y0 + r2)
    p.arcTo(x0, y0, x0 + 2*r2, y0 + 2*r2, startAng=180, extent=90)
    p.close()
    c.clipPath(p, stroke=0)
    draw_gradient_rect(c, x0, y0, cta_w, 30, "#3B82F6", "#EC4899", steps=30)
    c.restoreStore = c.restoreState
    c.restoreState()

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 12)
    c.drawCentredString(W/2, cta_y + 10, "¡Prueba la app en la T4 y danos tu opinion!")

    # ── FEEDBACK DESCRIPTION ──
    desc_y = cta_y - 20
    c.setFillColor(GRAY)
    c.setFont("Helvetica", 8.5)
    c.drawCentredString(W/2, desc_y,
        "Escanea, explora exposiciones cerca de ti y cuéntanos qué te parece.")

    # ── STARS DECO ──
    c.saveState()
    c.setFillColor(HexColor("#8B5CF6"))
    for sx2, sy2, sz in [(65, desc_y-15, 3), (W-65, desc_y-15, 3),
                          (55, desc_y-12, 1.5), (W-55, desc_y-12, 1.5)]:
        c.circle(sx2, sy2, sz, fill=1, stroke=0)
    c.restoreState()

    # ── BOTTOM BAND ──
    band_h = 72
    draw_rounded_rect(c, 20, 16, W - 40, band_h, 12,
                      fill=HexColor("#1C1C24"), stroke=BORDER, stroke_width=0.8)

    # Names
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 10.5)
    c.drawCentredString(W/4, 16 + band_h*0.62, "Blanca Muñoz")
    c.drawCentredString(3*W/4, 16 + band_h*0.62, "Carla Romero")

    c.setFillColor(GRAY)
    c.setFont("Helvetica", 7.5)
    c.drawCentredString(W/4, 16 + band_h*0.37, "Desarrolladora")
    c.drawCentredString(3*W/4, 16 + band_h*0.37, "Desarrolladora")

    # Vertical divider
    mid_x = W/2
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.8)
    c.line(mid_x, 22, mid_x, 16 + band_h - 6)

    # Brand accent dots
    for col, xp in [(BLUE, mid_x - 20), (PINK, mid_x + 20)]:
        c.setFillColor(col)
        c.circle(xp, 16 + band_h/2, 2.5, fill=1, stroke=0)

    # ── CORNER DECORATIONS ──
    c.saveState()
    c.setFillColor(Color(0.39, 0.51, 0.965, 0.08))
    c.circle(30, H - 30, 50, fill=1, stroke=0)
    c.setFillColor(Color(0.925, 0.286, 0.6, 0.08))
    c.circle(W - 30, H - 30, 40, fill=1, stroke=0)
    c.setFillColor(Color(0.39, 0.51, 0.965, 0.05))
    c.circle(W - 20, 100, 60, fill=1, stroke=0)
    c.restoreState()

    c.save()
    print(f"Poster saved to: {out_path}")

make_poster()
