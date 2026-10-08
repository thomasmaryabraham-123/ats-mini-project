# dashboard/models.py
from django.db import models

# Job model matching the 'jobs' table created by Java Swing
class Job(models.Model):
    title = models.CharField(max_length=200)
    required_skills = models.TextField()
    min_experience = models.IntegerField()

    class Meta:
        managed = False  # Django will NOT create or modify this table
        db_table = 'jobs'

    def __str__(self):
        return self.title

# Candidate model matching the 'candidates' table created by Java Swing
class Candidate(models.Model):
    name = models.CharField(max_length=200)
    email = models.CharField(max_length=200)
    skills = models.TextField()
    experience = models.IntegerField()
    job = models.ForeignKey(Job, on_delete=models.DO_NOTHING, db_column='job_id')
    match_score = models.FloatField()
    status = models.CharField(max_length=50, default='Applied')

    class Meta:
        managed = False  # Django will NOT create or modify this table
        db_table = 'candidates'

    def __str__(self):
        return self.name
