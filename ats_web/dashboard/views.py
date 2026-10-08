# dashboard/views.py
from django.shortcuts import render
from .models import Candidate

# Dashboard view: counts per status and candidates table sorted by match score
def dashboard(request):
    # Fixed list of statuses
    statuses = ['Applied', 'Shortlisted', 'Interviewed', 'Selected', 'Rejected']

    # Count of candidates per status
    status_counts = {}
    for s in statuses:
        status_counts[s] = Candidate.objects.filter(status=s).count()

    # All candidates sorted by match_score highest first
    candidates = Candidate.objects.select_related('job').order_by('-match_score')

    return render(request, 'dashboard.html', {
        'status_counts': status_counts,
        'candidates': candidates,
    })
