$(document).ready(function () {

  var ViewModel = function () {
    this.username = ko.observable('');
    this.password = ko.observable('');
    this.response = ko.observable('');

    this.login = () =>
      $.ajax({
        type: 'POST',
        url: '/api/login',
        data: JSON.stringify({
          username: this.username(),
          password: this.password(), }),
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        success: (response) => location.href = response.admin ? '/admin/users' : '/user/data',
        error: () => this.response('Login failed.'),
      });
  };

  ko.applyBindings(new ViewModel());
});
