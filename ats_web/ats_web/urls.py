# ats_web/urls.py
from django.urls import path
from dashboard.views import dashboard

urlpatterns = [
    # Map root URL "/" to dashboard view
    path('', dashboard, name='dashboard'),
]
